# Plugin signing

Local signing material for JetBrains Marketplace publishes. Generated files in this folder are **gitignored** — do not commit them.

| File | GitHub Actions secret |
|------|------------------------|
| `chain.crt` | `CERTIFICATE_CHAIN` |
| `private.pem` | `PRIVATE_KEY` |
| `password.txt` | `PRIVATE_KEY_PASSWORD` |

Paste each file’s **full contents** into the matching repository secret  
(**Settings → Secrets and variables → Actions**).

Regenerate (from repo root):

```bash
mkdir -p signing
openssl rand -base64 32 > signing/password.txt
chmod 600 signing/password.txt
openssl genpkey -aes-256-cbc -algorithm RSA \
  -out signing/private.pem \
  -pkeyopt rsa_keygen_bits:4096 \
  -pass file:signing/password.txt
openssl req -key signing/private.pem -passin file:signing/password.txt \
  -new -x509 -days 3650 -out signing/chain.crt \
  -subj "/CN=Copy Path for AI/O=bygo"
chmod 600 signing/private.pem
```

Docs: https://plugins.jetbrains.com/docs/intellij/plugin-signing.html
