import 'dart:math';

import 'package:hive_flutter/hive_flutter.dart';

/// Local persistent knowledge base for repeated user questions and AI answers.
///
/// Backed by Hive boxes acting like app-local tables:
/// - `keeply_kb_entries_v1`: rows for FAQ/query/knowledge
/// - `keeply_kb_meta_v1`: small metadata such as id counters
class KeeplyAiKnowledgeBase {
  KeeplyAiKnowledgeBase._();
  static final KeeplyAiKnowledgeBase instance = KeeplyAiKnowledgeBase._();

  static const String _entriesBoxName = 'keeply_kb_entries_v1';
  static const String _metaBoxName = 'keeply_kb_meta_v1';
  static const String _nextIdKey = 'next_id';

  Box<dynamic>? _entries;
  Box<dynamic>? _meta;

  Future<void> _ensureOpen() async {
    _entries ??= await Hive.openBox<dynamic>(_entriesBoxName);
    _meta ??= await Hive.openBox<dynamic>(_metaBoxName);
  }

  String _normalize(String s) => s.trim().toLowerCase().replaceAll(RegExp(r'\s+'), ' ');

  Future<int> _nextId() async {
    await _ensureOpen();
    final current = ((_meta!.get(_nextIdKey) as int?) ?? 1).clamp(1, 1 << 30);
    await _meta!.put(_nextIdKey, current + 1);
    return current;
  }

  Future<void> recordEntry({
    required String type, // faq | query | knowledge | ticket
    required String question,
    required String answer,
    String? relatedService,
    int incrementFrequencyBy = 1,
  }) async {
    final qNorm = _normalize(question);
    final aTrim = answer.trim();
    if (qNorm.isEmpty || aTrim.isEmpty) return;
    await _ensureOpen();

    final nowIso = DateTime.now().toIso8601String();
    dynamic hitKey;
    Map<String, dynamic>? hit;
    for (final k in _entries!.keys) {
      final raw = _entries!.get(k);
      if (raw is! Map) continue;
      final row = Map<String, dynamic>.from(raw);
      if ((row['type'] as String? ?? '') != type) continue;
      if ((row['questionNorm'] as String? ?? '') == qNorm) {
        hitKey = k;
        hit = row;
        break;
      }
    }

    if (hitKey != null && hit != null) {
      final freq = (hit['frequency'] as int? ?? 0) + max(1, incrementFrequencyBy);
      await _entries!.put(hitKey, {
        ...hit,
        'answer': aTrim,
        'frequency': freq,
        'relatedService': relatedService ?? hit['relatedService'],
        'updatedAt': nowIso,
      });
      return;
    }

    final id = await _nextId();
    await _entries!.put(id, {
      'id': id,
      'type': type,
      'question': question.trim(),
      'questionNorm': qNorm,
      'answer': aTrim,
      'frequency': max(1, incrementFrequencyBy),
      'relatedService': relatedService,
      'createdAt': nowIso,
      'updatedAt': nowIso,
    });
  }

  /// Try instant local answer for recurring questions before calling remote AI.
  Future<String?> findBestAnswer(
    String question, {
    String? relatedService,
    int minFrequency = 2,
  }) async {
    final qNorm = _normalize(question);
    if (qNorm.isEmpty) return null;
    await _ensureOpen();

    Map<String, dynamic>? best;
    var bestScore = -1.0;
    for (final k in _entries!.keys) {
      final raw = _entries!.get(k);
      if (raw is! Map) continue;
      final row = Map<String, dynamic>.from(raw);
      final rq = (row['questionNorm'] as String?) ?? '';
      if (rq.isEmpty) continue;
      final freq = (row['frequency'] as int?) ?? 0;
      if (freq < minFrequency) continue;

      var score = 0.0;
      if (rq == qNorm) score += 5;
      if (rq.contains(qNorm) || qNorm.contains(rq)) score += 2.5;
      final overlap = _tokenOverlap(rq, qNorm);
      score += overlap * 2;
      score += min(3, freq / 5);
      final svc = row['relatedService'] as String?;
      if (relatedService != null && relatedService.isNotEmpty && svc == relatedService) {
        score += 0.7;
      }

      if (score > bestScore) {
        bestScore = score;
        best = row;
      }
    }
    return best?['answer'] as String?;
  }

  double _tokenOverlap(String a, String b) {
    final sa = a.split(' ').where((e) => e.isNotEmpty).toSet();
    final sb = b.split(' ').where((e) => e.isNotEmpty).toSet();
    if (sa.isEmpty || sb.isEmpty) return 0;
    final inter = sa.intersection(sb).length.toDouble();
    return inter / max(sa.length, sb.length);
  }

  Future<List<Map<String, dynamic>>> topEntries({
    String? type,
    int limit = 20,
  }) async {
    await _ensureOpen();
    final rows = <Map<String, dynamic>>[];
    for (final k in _entries!.keys) {
      final raw = _entries!.get(k);
      if (raw is! Map) continue;
      final row = Map<String, dynamic>.from(raw);
      if (type != null && type.isNotEmpty && row['type'] != type) continue;
      rows.add(row);
    }
    rows.sort((a, b) => (b['frequency'] as int? ?? 0).compareTo(a['frequency'] as int? ?? 0));
    return rows.take(max(1, limit)).toList();
  }
}

