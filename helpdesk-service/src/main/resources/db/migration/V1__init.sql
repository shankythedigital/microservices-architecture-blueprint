-- Helpdesk Service Database Schema
-- All tables extend BaseEntity which provides: created_by, created_at, updated_by, updated_at, active

-- Issues table
CREATE TABLE IF NOT EXISTS issues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    related_service VARCHAR(50) NOT NULL,
    reported_by VARCHAR(255) NOT NULL,
    assigned_to VARCHAR(255),
    current_support_level VARCHAR(10),
    initial_support_level VARCHAR(10),
    assigned_at TIMESTAMP NULL,
    first_response_at TIMESTAMP NULL,
    resolution TEXT,
    resolved_at TIMESTAMP NULL,
    escalation_count INT DEFAULT 0,
    last_escalated_at TIMESTAMP NULL,
    -- BaseEntity fields
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE
);

-- FAQs table
CREATE TABLE IF NOT EXISTS faqs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question VARCHAR(500) NOT NULL,
    answer TEXT NOT NULL,
    related_service VARCHAR(50) NOT NULL,
    category VARCHAR(100) NOT NULL,
    view_count INT DEFAULT 0,
    helpful_count INT DEFAULT 0,
    -- BaseEntity fields
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE
);

-- Queries table
CREATE TABLE IF NOT EXISTS queries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question VARCHAR(500) NOT NULL,
    answer TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    related_service VARCHAR(50) NOT NULL,
    asked_by VARCHAR(255) NOT NULL,
    answered_by VARCHAR(255),
    answered_at TIMESTAMP NULL,
    -- BaseEntity fields
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE
);

-- Chatbot sessions table
CREATE TABLE IF NOT EXISTS chatbot_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    -- BaseEntity fields
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE
);

-- Chatbot messages table
CREATE TABLE IF NOT EXISTS chatbot_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    -- BaseEntity fields
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (session_id) REFERENCES chatbot_sessions(id) ON DELETE CASCADE
);

-- Service knowledge table
CREATE TABLE IF NOT EXISTS service_knowledge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service VARCHAR(50) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(100) NOT NULL,
    api_endpoints TEXT,
    common_issues TEXT,
    troubleshooting_steps TEXT,
    -- BaseEntity fields
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE
);

-- Create indexes for better performance
CREATE INDEX idx_issues_status ON issues(status);
CREATE INDEX idx_issues_service ON issues(related_service);
CREATE INDEX idx_issues_reported_by ON issues(reported_by);
CREATE INDEX idx_issues_assigned_to ON issues(assigned_to);

CREATE INDEX idx_faqs_service ON faqs(related_service);
CREATE INDEX idx_faqs_category ON faqs(category);

CREATE INDEX idx_queries_status ON queries(status);
CREATE INDEX idx_queries_service ON queries(related_service);
CREATE INDEX idx_queries_asked_by ON queries(asked_by);

CREATE INDEX idx_chatbot_sessions_user_id ON chatbot_sessions(user_id);
CREATE INDEX idx_chatbot_sessions_session_id ON chatbot_sessions(session_id);

CREATE INDEX idx_chatbot_messages_session_id ON chatbot_messages(session_id);

CREATE INDEX idx_service_knowledge_service ON service_knowledge(service);
CREATE INDEX idx_service_knowledge_category ON service_knowledge(category);

-- Escalation Matrix table
CREATE TABLE IF NOT EXISTS escalation_matrix (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    related_service VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    support_level VARCHAR(10) NOT NULL,
    initial_assignment_level VARCHAR(10) NOT NULL,
    escalate_to_level VARCHAR(10),
    escalation_time_minutes INT,
    response_time_minutes INT NOT NULL,
    resolution_time_minutes INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    -- BaseEntity fields
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    UNIQUE KEY uk_escalation_matrix (related_service, priority, support_level)
);

-- Issue Escalations table
CREATE TABLE IF NOT EXISTS issue_escalations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    issue_id BIGINT NOT NULL,
    from_level VARCHAR(10),
    to_level VARCHAR(10) NOT NULL,
    escalated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    escalation_reason TEXT,
    escalated_by VARCHAR(255),
    -- BaseEntity fields
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (issue_id) REFERENCES issues(id) ON DELETE CASCADE
);

-- SLA Tracking table
CREATE TABLE IF NOT EXISTS sla_tracking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    issue_id BIGINT NOT NULL UNIQUE,
    response_time_minutes INT,
    resolution_time_minutes INT,
    first_response_at TIMESTAMP NULL,
    resolved_at TIMESTAMP NULL,
    response_sla_met BOOLEAN,
    resolution_sla_met BOOLEAN,
    response_sla_breach_at TIMESTAMP NULL,
    resolution_sla_breach_at TIMESTAMP NULL,
    actual_response_time_minutes INT,
    actual_resolution_time_minutes INT,
    -- BaseEntity fields
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (issue_id) REFERENCES issues(id) ON DELETE CASCADE
);

-- Additional indexes for escalation and SLA
CREATE INDEX idx_issues_support_level ON issues(current_support_level);
CREATE INDEX idx_issues_escalation_count ON issues(escalation_count);
CREATE INDEX idx_escalation_matrix_service_priority ON escalation_matrix(related_service, priority);
CREATE INDEX idx_escalation_matrix_active ON escalation_matrix(is_active);
CREATE INDEX idx_issue_escalations_issue_id ON issue_escalations(issue_id);
CREATE INDEX idx_issue_escalations_escalated_at ON issue_escalations(escalated_at);
CREATE INDEX idx_sla_tracking_issue_id ON sla_tracking(issue_id);
CREATE INDEX idx_sla_tracking_breaches ON sla_tracking(response_sla_met, resolution_sla_met);

