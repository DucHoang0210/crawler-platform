ALTER TABLE crawler_domain_config
ADD COLUMN price_selector VARCHAR(255),
ADD COLUMN product_name_selector VARCHAR(255),
ADD COLUMN title_selector VARCHAR(255),
ADD COLUMN description_selector VARCHAR(255);