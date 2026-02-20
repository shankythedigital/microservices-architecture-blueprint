# LLM and Agentic AI Document Extraction

When any document is uploaded, the system can **scan** it (OCR/text extraction), **train** the extraction by using an LLM agent that follows a strict schema, and **extract** asset data, returning the result in **JSON format**.

## Endpoint

- **POST** `/api/asset/v1/llm-extraction/extract`
- **Content-Type:** `multipart/form-data`
- **Parameters:**
  - `file` (required): Document file (image, PDF, Word, Excel, PowerPoint)
  - `documentType` (optional): Override detected type (e.g. `INVOICE`, `WARRANTY_CARD`, `AMC_DOCUMENT`, `SPEC_SHEET`, `MANUAL`)

**Response:** JSON (wrapped in `ResponseWrapper`) with extracted asset data:

- `documentType`, `extractedTextPreview`, `processingTimeMs`
- Asset: `assetName`, `serialNumber`, `categoryName`, `subCategoryName`, `makeName`, `modelName`, `brand`, `description`, `assetStatus`
- Purchase: `purchaseDate`, `purchasePrice`, `invoiceNumber`, `invoiceDate`, `billNumber`, `vendorName`, `outletName`, `currency`, etc.
- `warranty`: `warrantyStatus`, `warrantyProvider`, `startDate`, `endDate`, `duration`, `terms`
- `amc`: `amcStatus`, `provider`, `startDate`, `endDate`, `duration`
- `componentNames`: array of component names
- `extractionMethod`: `"LLM_AGENT"`, `confidence`: 0.0–1.0

## Configuration

In `application.yml` (or environment variables):

```yaml
app:
  llm:
    enabled: true
    api-url: https://api.openai.com/v1   # or Azure OpenAI / Ollama URL
    api-key: ${OPENAI_API_KEY:}
    model: gpt-4o-mini
    max-tokens: 4096
    timeout-seconds: 60
```

- **OpenAI:** Set `OPENAI_API_KEY` or `app.llm.api-key`.
- **Ollama (local):** e.g. `api-url: http://localhost:11434/v1`, `model: llama3.2`, leave `api-key` empty if not required.
- **Azure OpenAI:** Use your Azure endpoint as `api-url` and the Azure API key as `api-key`.

If `app.llm.enabled` is `false` or `api-key` is not set, the endpoint still runs OCR and returns a result with empty/null extracted fields and `confidence: 0`.

## Flow

1. **Upload** → Document is received.
2. **Scan** → Text is extracted via existing OCR (Tesseract for images, PDFBox/POI for PDF/Office).
3. **Train / Agent** → Document type is detected (or overridden). The LLM is prompted with the document text and a fixed JSON schema so the model “learns” to output consistent asset fields.
4. **Extract** → LLM returns a JSON object; the service parses it into `LlmAssetExtractionResult`.
5. **Showcase** → Response is returned in JSON format (the `data` field is the extracted asset object).

To **persist** the extracted data (create asset, warranty, AMC, etc.), use the existing intelligent extraction endpoint with the same file: **POST** `/api/asset/v1/intelligent-extraction/extract`.
