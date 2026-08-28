
-- 1. Bảng lưu dữ liệu cào
CREATE TABLE IF NOT EXISTS scraped_data (
                                            id BIGSERIAL PRIMARY KEY,
                                            product_id VARCHAR(255) NOT NULL,
    title VARCHAR(500) NOT NULL,
    url TEXT NOT NULL,
    current_price DECIMAL(15, 2),
    raw_data JSONB,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
    );

-- 2. Bảng lịch sử giá
CREATE TABLE IF NOT EXISTS price_histories (
                                               id BIGSERIAL PRIMARY KEY,
                                               scraped_data_id BIGINT NOT NULL,
                                               price DECIMAL(15, 2) NOT NULL,
    recorded_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_price_history_scraped_data
    FOREIGN KEY (scraped_data_id)
    REFERENCES scraped_data(id)
                          ON DELETE CASCADE
    );

-- 3. Bảng bài viết/bảng tin thương hiệu
CREATE TABLE IF NOT EXISTS brand_posts (
                                           id BIGSERIAL PRIMARY KEY,
                                           post_id VARCHAR(255) UNIQUE NOT NULL,
    author VARCHAR(255),
    content TEXT,
    engagement_count INT DEFAULT 0,
    posted_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
    );

-- Tạo index hỗ trợ truy vấn nhanh
CREATE INDEX IF NOT EXISTS idx_scraped_data_product_id ON scraped_data(product_id);
CREATE INDEX IF NOT EXISTS idx_price_histories_scraped_id ON price_histories(scraped_data_id);