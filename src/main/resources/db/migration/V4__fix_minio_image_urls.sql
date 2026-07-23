-- V4: Fix MinIO image URLs to use browser-accessible /storage/ proxy path
UPDATE listings
SET image_url = '/storage' || REPLACE(image_url, 'http://minio:9000', '')
WHERE image_url LIKE 'http://minio:9000%';
