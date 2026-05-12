FROM python:3.12-slim

WORKDIR /docs

COPY docs-requirements.txt ./
RUN pip install --no-cache-dir -r docs-requirements.txt

EXPOSE 8000

CMD ["mkdocs", "serve", "--dev-addr", "0.0.0.0:8000"]
