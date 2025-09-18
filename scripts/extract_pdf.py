import sys
import pdfplumber

def extract(input_path, output_path='extracted_pdf.txt'):
    text_parts = []
    with pdfplumber.open(input_path) as pdf:
        for i, page in enumerate(pdf.pages):
            t = page.extract_text()
            if t and t.strip():
                text_parts.append(f"\n--- PAGE {i+1} ---\n")
                text_parts.append(t)
    res = '\n'.join(text_parts)
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(res)
    print(f'WROTE {output_path} ({len(res)} chars)')

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print('Usage: extract_pdf.py <input-pdf> [output-txt]')
        sys.exit(1)
    inp = sys.argv[1]
    out = sys.argv[2] if len(sys.argv) > 2 else 'extracted_pdf.txt'
    extract(inp, out)
