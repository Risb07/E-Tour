import { useRef, useState } from "react";
import { UploadCloud, FileSpreadsheet, CheckCircle2, AlertTriangle, Download } from "lucide-react";
import { uploadTourExcel, downloadTourExcelTemplate } from "../../services/adminService";
import { useToast } from "../../hooks/useToast";
import Button from "../../components/common/Button";
import Badge from "../../components/ui/Badge";

// Mirrors TourExcelTemplateGenerator.NOTES on the backend.
const VALIDATION_RULES = [
  { column: "title", detail: "Required, max 200 chars" },
  { column: "description", detail: "Optional free text" },
  { column: "durationDays", detail: "Required, whole number ≥ 1" },
  { column: "basePrice", detail: "Required, > 0, digits only" },
  { column: "tourCode", detail: "ADV, INT, DEV or DOM" },
  { column: "categoryName", detail: "Must match an existing category" },
  { column: "status", detail: "ACTIVE or DRAFT (default DRAFT)" },
];

export default function AdminExcelUploadPage() {
  const [file, setFile] = useState(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [result, setResult] = useState(null);
  const [isDownloading, setIsDownloading] = useState(false);
  const inputRef = useRef(null);
  const { showToast } = useToast();

  async function handleDownloadTemplate() {
    setIsDownloading(true);
    try {
      await downloadTourExcelTemplate();
    } catch (err) {
      showToast(err.message || "Couldn't download the template.", "error");
    } finally {
      setIsDownloading(false);
    }
  }

  function handleFileSelect(selected) {
    if (!selected) return;
    setFile(selected);
    setResult(null);
  }

  async function handleUpload() {
    if (!file) return;
    setIsUploading(true);
    try {
      const data = await uploadTourExcel(file);
      setResult(data);
      showToast(`Processed ${data.totalRows} rows: ${data.successRows} succeeded.`, data.failedRows > 0 ? "info" : "success");
    } catch (err) {
      showToast(err.message || "Upload failed.", "error");
    } finally {
      setIsUploading(false);
    }
  }

  return (
    <div className="max-w-2xl">
      <h1 className="font-display text-2xl font-bold text-ink-900">Bulk Tour Upload</h1>
      <p className="mt-1 text-sm text-ink-500">
        Upload an Excel file to create multiple tours at once.
      </p>

      {/* Template is generated server-side from the same column constant the
          importer parses, so it can never drift from what's accepted. */}
      <div className="mt-5 rounded-card border border-ink-100 bg-white p-5 shadow-soft">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <h2 className="font-semibold text-ink-900">Start from the template</h2>
            <p className="mt-1 text-sm text-ink-500">
              Includes the exact column order, an example row, and a sheet of validation rules.
            </p>
          </div>
          <Button variant="outline" icon={Download} isLoading={isDownloading} onClick={handleDownloadTemplate}>
            Download template
          </Button>
        </div>

        <dl className="mt-4 grid grid-cols-1 gap-x-6 gap-y-2 border-t border-ink-100 pt-4 text-xs sm:grid-cols-2">
          {VALIDATION_RULES.map((rule) => (
            <div key={rule.column} className="flex gap-2">
              <dt className="shrink-0 font-mono font-semibold text-ink-700">{rule.column}</dt>
              <dd className="text-ink-500">{rule.detail}</dd>
            </div>
          ))}
        </dl>

        <p className="mt-4 text-xs text-ink-400">
          Row 1 must stay as the header. Invalid rows are reported individually and skipped — valid
          rows still import.
        </p>
      </div>

      <div
        onDragOver={(e) => {
          e.preventDefault();
          setIsDragging(true);
        }}
        onDragLeave={() => setIsDragging(false)}
        onDrop={(e) => {
          e.preventDefault();
          setIsDragging(false);
          handleFileSelect(e.dataTransfer.files[0]);
        }}
        onClick={() => inputRef.current?.click()}
        className={`mt-6 flex cursor-pointer flex-col items-center gap-3 rounded-card border-2 border-dashed p-10 text-center transition-colors ${
          isDragging ? "border-amber-400 bg-amber-50" : "border-ink-200 bg-white hover:border-ink-300"
        }`}
      >
        <input
          ref={inputRef}
          type="file"
          accept=".xlsx,.xls"
          className="hidden"
          onChange={(e) => handleFileSelect(e.target.files[0])}
        />
        {file ? (
          <>
            <FileSpreadsheet className="h-10 w-10 text-amber-500" />
            <p className="font-semibold text-ink-900">{file.name}</p>
            <p className="text-xs text-ink-400">Click or drop to replace</p>
          </>
        ) : (
          <>
            <UploadCloud className="h-10 w-10 text-ink-300" />
            <p className="font-semibold text-ink-700">Drag & drop your Excel file here</p>
            <p className="text-xs text-ink-400">or click to browse (.xlsx, .xls)</p>
          </>
        )}
      </div>

      <Button onClick={handleUpload} disabled={!file} isLoading={isUploading} className="mt-5">
        Upload and process
      </Button>

      {result && (
        <div className="mt-8 rounded-card bg-white p-6 shadow-card">
          <div className="flex items-center gap-3">
            {result.failedRows === 0 ? (
              <CheckCircle2 className="h-6 w-6 text-emerald-500" />
            ) : (
              <AlertTriangle className="h-6 w-6 text-amber-500" />
            )}
            <div>
              <p className="font-semibold text-ink-900">
                {result.successRows} of {result.totalRows} rows imported
              </p>
              <Badge variant={result.failedRows === 0 ? "success" : "warning"}>{result.status}</Badge>
            </div>
          </div>

          {result.errors?.length > 0 && (
            <div className="mt-4 max-h-64 overflow-y-auto rounded-xl bg-red-50 p-4">
              <p className="mb-2 text-xs font-bold uppercase tracking-wide text-red-600">Row errors</p>
              <ul className="flex flex-col gap-1 text-xs text-red-700">
                {result.errors.map((err, i) => (
                  <li key={i}>{err}</li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
