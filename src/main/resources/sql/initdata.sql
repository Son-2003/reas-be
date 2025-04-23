-- Insert data into the ROLE table
INSERT INTO public."ROLE"
("STATUS_ENTITY", "USR_LOG_I", "DTE_LOG_I", "DTE_LOG_U", "USR_LOG_U", "VERSION", "NAME")
VALUES
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'ROLE_ADMIN'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'ROLE_STAFF'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'ROLE_RESIDENT');

-- Insert data into the BRAND table
INSERT INTO public."BRAND"
("STATUS_ENTITY", "USR_LOG_I", "DTE_LOG_I", "DTE_LOG_U", "USR_LOG_U", "VERSION", "BRAND_NAME", "DESCRIPTION", "IMAGE")
VALUES
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Samsung', 'Leading electronics brand known for phones, TVs, and appliances.', 'abc'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'LG', 'Electronics and home appliances brand.', 'abc'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Philips', 'Global sportswear and footwear brand.', 'abc'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Xiaomi', 'Innovative sportswear brand.', 'abc'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Eufy', 'Fashion retailer offering trendy clothing.', 'abc'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Dyson', 'Famous for affordable and stylish furniture.', 'abc'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Panasonic', 'Sportswear and footwear brand.', 'abc'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'TOTO', 'Affordable clothing and accessories brand.', 'abc'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Bosch', 'Brand known for reliable home appliances.', 'abc'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Sony', 'Electronics and entertainment products leader.', 'abc');

-- Insert data into the CATEGORY table
INSERT INTO public."CATEGORY"
("STATUS_ENTITY", "USR_LOG_I", "DTE_LOG_I", "DTE_LOG_U", "USR_LOG_U", "VERSION", "CATEGORY_NAME", "DESCRIPTION", "TYPE_ITEM")
VALUES
    -- KITCHEN_APPLIANCES ("KITC")
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Refrigerators', 'Including compact and multi-door models.', 'KITC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Rice Cookers', 'Essential for daily cooking.', 'KITC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Induction Cookers', 'Popular for fast and efficient cooking.', 'KITC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Microwave Ovens', 'Widely used for reheating and quick meals.', 'KITC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Electric Kettles', 'For boiling water for tea and coffee.', 'KITC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Water Purifiers', 'Important for ensuring safe drinking water.', 'KITC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Blenders, Mixer & Food Processors', 'For preparing smoothies, sauces, and traditional recipes.', 'KITC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Dishwashers', 'Increasingly popular in urban apartments.', 'KITC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Coffee Makers', 'Catering to both local and international coffee trends.', 'KITC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Air Fryers', 'Gaining traction as a healthier cooking option.', 'KITC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Others', 'Other appliances', 'KITC'),

    -- CLEANING_LAUNDRY_APPLIANCES ("CLLA")
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Washing Machines', 'Both front-load and top-load types.', 'CLLA'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Washer-Dryer Combos', 'Useful in limited urban spaces.', 'CLLA'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Clothes Dryers', 'For apartment dwellers without outdoor drying space.', 'CLLA'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Steam Irons', 'For quick and effective wrinkle removal.', 'CLLA'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Robotic Vacuum Cleaners', 'An emerging trend in high-end urban homes.', 'CLLA'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Upright/Handheld Vacuum Cleaners', 'For everyday cleaning tasks.', 'CLLA'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Steam Mops', 'A modern approach to floor cleaning.', 'CLLA'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Portable Washing Machines', 'Ideal for smaller households or dorms.', 'CLLA'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Carpet Cleaners', 'For homes with area rugs or carpets.', 'CLLA'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Lint Removers/Fabric Shavers', 'To maintain clothing and upholstery quality.', 'CLLA'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Others', 'Other appliances', 'CLLA'),

    -- COOLING_HEATING_APPLIANCES ("COHE")
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Split Air Conditioners', 'Standard for most urban homes.', 'COHE'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Window Air Conditioners', 'A cost-effective cooling option.', 'COHE'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Portable Air Conditioners', 'For added flexibility in cooling.', 'COHE'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Ceiling Fans', 'Popular and energy-efficient.', 'COHE'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Table & Floor Fans', 'Common in many households.', 'COHE'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Air Coolers/Evaporative Coolers', 'Suited for certain regions.', 'COHE'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Smart Air Conditioners', 'With remote and app control features.', 'COHE'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Air Purifiers', 'Improving sleep quality by filtering air.', 'BEDR'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Humidifiers', 'Depending on the local microclimate.', 'BEDR'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Dehumidifiers', 'Depending on the local microclimate.', 'BEDR'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Energy-Efficient Cooling Solutions', 'Models designed to lower electricity bills.', 'COHE'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Others', 'Other appliances', 'COHE'),

    -- ELECTRONICS_ENTERTAINMENT_DEVICES ("ELEC")
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Smart TVs', 'A centerpiece for modern living rooms.', 'ELEC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Home Theater Systems', 'For immersive audio-visual experiences.', 'ELEC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Soundbars', 'To enhance TV audio in compact spaces.', 'ELEC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Streaming Devices', 'Devices like Roku, Apple TV, or local alternatives.', 'ELEC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Gaming Consoles', 'Popular among youth and tech-savvy consumers.', 'ELEC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Digital Cameras', 'Although smartphones are often preferred.', 'ELEC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Portable Bluetooth Speakers', 'For on-the-go or outdoor use.', 'ELEC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Set-Top Boxes', 'For cable and satellite TV integration.', 'ELEC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Blu-ray/DVD Players', 'Still in use for certain content.', 'ELEC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Virtual Reality Headsets', 'A niche but growing interest.', 'ELEC'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Others', 'Other appliances', 'ELEC'),

    -- LIGHTING_SECURITY_DEVICES ("LISE")
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Smart LED Bulbs', 'Energy-efficient with app control.', 'LISE'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'LED Light Strips & Panels', 'For decorative and ambient lighting.', 'LISE'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Security Cameras', 'Essential for modern home security.', 'LISE'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Doorbells', 'For added safety and convenience.', 'LISE'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Motion Sensors', 'Integrated into smart home setups.', 'LISE'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Alarm Systems', 'Ranging from basic to advanced.', 'LISE'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Smart Locks', 'Enhancing door security.', 'LISE'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Outdoor Floodlights', 'For well-lit surroundings.', 'LISE'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Wireless Security Sensors', 'For windows and doors.', 'LISE'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Home Automation Hubs', 'Centralizing control of multiple devices.', 'LISE'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Others', 'Other appliances', 'LISE'),

    -- BEDROOM_APPLIANCES ("BEDR")
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Bedside Lamps', 'With adjustable brightness.', 'BEDR'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Bed', 'Comfortable thing to sleep on.', 'BEDR'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Blanket', 'The part of a bed, made of a strong cloth cover filled with firm material, that makes the bed comfortable to lie on.', 'BEDR'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Personal/Mini Fans', 'For personal cooling.', 'BEDR'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Smart Alarm Clocks', 'With gentle wake-up features.', 'BEDR'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Sleep Trackers', 'For monitoring sleep quality.', 'BEDR'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'LED Reading Lights', 'For night-time reading.', 'BEDR'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Aroma Diffusers', 'To create a relaxing environment.', 'BEDR'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Electric Mosquito Repellers', 'Essential in tropical regions.', 'BEDR'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'White Noise Machines', 'For blocking out disruptive sounds.', 'BEDR'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Others', 'Other appliances', 'BEDR'),

    -- LIVING_ROOM_APPLIANCES ("LIVI")
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Sofa Set', 'A comfortable and stylish sofa set perfect for a cozy living room.', 'LIVI'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Armchair', 'Elegant armchairs that add both comfort and charm to your living space.', 'LIVI'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Coffee Table', 'A versatile coffee table ideal for holding drinks, magazines, and decor.', 'LIVI'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Side Table', 'Convenient side tables to complement seating areas and provide extra surface space.', 'LIVI'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Ottoman', 'A multifunctional ottoman that can serve as a footrest or additional seating.', 'LIVI'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Others', 'Other appliances', 'LIVI'),

    -- BATHROOM_APPLIANCES ("BATH")
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Electric Toothbrushes', 'For improved oral hygiene.', 'BATH'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Instant/Tankless Water Heaters', 'Providing hot water on demand.', 'BATH'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Hair Dryers', 'A common necessity.', 'BATH'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Electric Shavers/Hair Trimmers', 'For personal grooming.', 'BATH'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Bathroom Ventilation Fans', 'To reduce humidity and mold.', 'BATH'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Toilet Seats', 'Gaining popularity in modern bathrooms.', 'BATH'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Heated Towel Racks', 'More common in upscale settings.', 'BATH'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Electric Faucets', 'For water efficiency and hygiene.', 'BATH'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Smart Mirrors', 'Offering integrated lighting and information.', 'BATH'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Digital Bathroom Scales', 'For personal health tracking.', 'BATH'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Others', 'Other appliances', 'BATH');

-- Insert data into the USER table
INSERT INTO public."USER"
("STATUS_ENTITY", "USR_LOG_I", "DTE_LOG_I", "DTE_LOG_U", "USR_LOG_U", "VERSION", "EMAIL", "FULL_NAME", "GENDER", "IMAGE", "IS_FIRST_LOGIN", "PASSWORD", "PHONE", "USER_NAME", "ROLE_ID")
VALUES
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'admin@reas.vn', 'Admin', 'MALE',  NULL, false, '$2a$10$uEJouiW.IpLccYE5koowSexNlWLsvxUqWJGwbdszt/ANPudcpinHi', '1234567890', 'admin', 1),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'ducson@reas.vn', 'Duc Son', 'MALE', NULL, false, '$2a$10$uEJouiW.IpLccYE5koowSexNlWLsvxUqWJGwbdszt/ANPudcpinHi', '0987654321', 'ducson', 2),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'tiendung@reas.vn', 'Tien Dung', 'MALE', NULL, false, '$2a$10$uEJouiW.IpLccYE5koowSexNlWLsvxUqWJGwbdszt/ANPudcpinHi', '0912345678', 'tiendung', 3),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'ducanh@reas.vn', 'Duc Anh', 'MALE', NULL, false, '$2a$10$uEJouiW.IpLccYE5koowSexNlWLsvxUqWJGwbdszt/ANPudcpinHi', '0922334455', 'ducanh', 3),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'trangthao@reas.vn', 'Trang Thao', 'FEMALE', NULL, false, '$2a$10$uEJouiW.IpLccYE5koowSexNlWLsvxUqWJGwbdszt/ANPudcpinHi', '0933445566', 'trangthao', 3),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'minhquan@reas.vn', 'Minh Quan', 'FEMALE', NULL, false, '$2a$10$uEJouiW.IpLccYE5koowSexNlWLsvxUqWJGwbdszt/ANPudcpinHi', '0944556677', 'minhquan', 3),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'hiennhu@reas.vn', 'Hien Nhu', 'MALE', NULL, false, '$2a$10$uEJouiW.IpLccYE5koowSexNlWLsvxUqWJGwbdszt/ANPudcpinHi', '0955667788', 'hiennhu', 3),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'nguyentiendung2003@gmail.com', 'Nguyen Van Long', 'MALE', NULL, false, '$2a$10$.U5VdTwq8wyVyqhJc.rhI.dLaX4b1Ohc9O3Zq.4qeXuxuemf6tY6W', '0955667788', 'nguyentiendung2003', 3),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'dungntse171710@fpt.edu.vn', 'Nguyen Van Duc', 'MALE', NULL, false, '$2a$10$Lnw6uGVznXJ8v08ejBpZuepIoFfc0wlwCjI7WKpnmxFQSCyA0KqZa', '0955667788', 'dungntse171710', 3);

-- Insert data into the LOCATION table
INSERT INTO public."LOCATION"
("STATUS_ENTITY", "USR_LOG_I", "DTE_LOG_I", "DTE_LOG_U", "USR_LOG_U", "VERSION", "AREA", "CITY", "CLUSTER", "DISTRICT", "PROVINCE", "WARD")
VALUES
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Vinhomes Grand Park', 'Ho Chi Minh City', 'Vinhomes', 'District 9', 'Ho Chi Minh', 'Long Thanh My'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Masteri Thao Dien', 'Ho Chi Minh City', 'Masteri', 'District 2', 'Ho Chi Minh', 'Thao Dien'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Cityland Park Hills', 'Ho Chi Minh City', 'Cityland', 'Go Vap', 'Ho Chi Minh', 'Phu Nhuan'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'The Ascent', 'Ho Chi Minh City', 'The Ascent', 'District 2', 'Ho Chi Minh', 'Binh An'),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Saigon Pearl', 'Ho Chi Minh City', 'Saigon Pearl', 'Binh Thanh', 'Ho Chi Minh', '22 Ward');

-- Insert data into the USER_LOCATION table
INSERT INTO public."USER_LOCATION"
("STATUS_ENTITY", "USR_LOG_I", "DTE_LOG_I", "DTE_LOG_U", "USR_LOG_U", "VERSION", "IS_PRIMARY", "SPECIFIC_ADDRESS", "LOCATION_ID", "USER_ID", "LATITUDE", "LONGITUDE")
VALUES
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, true, 'Cổng Huỳnh Thúc Kháng, Công viên Indira Gandhi, Huỳnh Thúc Kháng, Thành Công, Ba Đình, Hà Nội', 1, 3, 21.017938, 105.812312),  -- Ti Dung - Vinhomes Grand Park
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, false, '123 Lê Quang Định, Phường 14, Bình Thạnh, Hồ Chí Minh', 2, 3, 10.80685, 106.696439),  -- Ti Dung - Masteri Thao Dien
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, false, 'Mai Chí Thọ, Giang Biên, Long Biên, Hà Nội', 3, 3, 21.028511, 105.804817),  -- Ti Dung - Cityland Park Hills
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, false, 'Lê Văn Duyệt, Phường 1, Bình Thạnh, Hồ Chí Minh', 4, 3, 10.795252, 106.696315),  -- Ti Dung - The Ascent
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, true, 'Nguyễn Văn Đậu, Phường 5, Bình Thạnh, Hồ Chí Minh', 1, 4, 10.810762, 106.690142),  -- Duc Anh - Vinhomes Grand Park
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, false, 'Bạch Đằng, Phường 2, Tân Bình, Hồ Chí Minh', 5, 4, 10.810762, 106.690142),  -- Duc Anh - Saigon Pearl
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, true, 'Ngã Tư Hàng Xanh, Phường 25, Bình Thạnh, Hồ Chí Minh', 2, 5, 10.801562, 106.711473),  -- Trang Thao - Masteri Thao Dien
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, false, 'Cây xăng, Xô Viết Nghệ Tĩnh, phường 21, Bình Thạnh, Hồ Chí Minh', 1, 5, 10.79586, 106.710926),  -- Trang Thao - Vinhomes Grand Park
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, true, 'Đinh Bộ Lĩnh, Phường 26, Bình Thạnh, Hồ Chí Minh', 3, 6, 10.811947, 106.707155),  -- Minh Quan - Cityland Park Hills
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, false, 'Bùi Đình Túy, Phường 24, Bình Thạnh, Hồ Chí Minh', 4, 6, 10.807833, 106.707155),  -- Minh Quan - The Ascent
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, true, 'Căn hộ, Điện Biên Phủ, phường 3, Quận 3, Hồ Chí Minh', 4, 7, 10.772173, 106.678872),  -- Hien Nhu - The Ascent
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, false, '442 Lê Quang Định, Phường 11, Bình Thạnh, Hồ Chí Minh', 5, 7, 10.812053, 106.69077);  -- Hien Nhu - Saigon Pearl

INSERT INTO public."DESIRED_ITEM"
("STATUS_ENTITY", "USR_LOG_I", "DTE_LOG_I", "DTE_LOG_U", "USR_LOG_U", "VERSION", "CONDITION_ITEM", "MAX_PRICE", "MIN_PRICE", "BRAND_ID", "CATEGORY_ID", "DESCRIPTION")
VALUES
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, NULL, 10000000, 900000, 9, 7, 'Find one with multiple speed settings'), -- LG Smart TV 55 Inch
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'LNEW', NULL, 0, NULL, NULL, 'Find front load washer'), -- TOTO Smart Toilet Seat
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, NULL, 4000000, 200000, NULL, NULL, 'Need a smart LED lightbulb'), -- Bosch Kitchen Mixer
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, NULL, 30000000, 2000000, NULL, NULL, 'Finding some excellent condition or like new air fryer'); -- Eufy Video Doorbell

INSERT INTO public."ITEM"
("STATUS_ENTITY", "USR_LOG_I", "DTE_LOG_I", "DTE_LOG_U", "USR_LOG_U", "VERSION", "ITEM_NAME", "DESCRIPTION", "APPROVED_TIME", "EXPIRED_TIME", "CONDITION_ITEM", "STATUS_ITEM", "CATEGORY_ID", "BRAND_ID", "IMAGE_URL", "OWNER_ID", "USER_LOCATION_ID", "TYPE_EXCHANGE", "PRICE", "DESIRED_ITEM_ID", "IS_MONEY_ACCEPTED")
VALUES
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Philips Air Fryer', 'Oil-free fryer with rapid air technology.', '2025-03-13 12:00:00', '2025-04-10 12:00:00','EXCE', 'AVAI', 10, 3, 'https://pisces.bbystatic.com/image2/BestBuy_US/images/products/6369/6369449_rd.jpg', 3, 1, 'OPEN', 250000, NULL, TRUE),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Samsung Front Load Washer', '9kg washing machine with eco-bubble technology.', '2025-03-14 12:00:00', '2025-04-28 12:00:00','LNEW', 'AVAI', 12, 1, 'https://pisces.bbystatic.com/image2/BestBuy_US/images/products/6323/6323149_sd.jpg', 3, 2, 'OPEN', 7500000, NULL, TRUE),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Dyson Air Purifier', 'Removes 99.97% of allergens and pollutants.', NULL, NULL, 'GOOD', 'PEND', 30, 6, 'https://dyson-h.assetsadobe2.com/is/image/content/dam/dyson/leap-petite-global/markets/thai/products/ec/tp07-bn-hepa13-pdp.png', 3, 3, 'OPEN', 4500000, NULL, FALSE),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Sony Bluetooth Speaker', 'Portable speaker with deep bass and 12-hour battery life.', '2025-02-13 12:00:00', '2025-04-13 12:00:00','EXCE', 'AVAI', 41, 10, 'https://static.bhphoto.com/images/images500x500/1590584805_1566561.jpg', 4, 5, 'OPEN', 1500000, NULL, TRUE),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Xiaomi Smart LED Bulb', 'Dimmable LED bulb with smart home integration.', '2025-03-03 12:00:00', '2025-04-03 12:00:00', 'LNEW', 'NLFE', 46, 4, 'https://www.mistorechile.cl/wp-content/uploads/2024/03/led-bulb-white-and-color-4-0021c55c-7990-4996-8b36-857ca8236398.png', 4, 6, 'OPEN', 200000, NULL, FALSE),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Panasonic Electric Blanket', 'Soft and cozy heated blanket with adjustable temperature.', '2025-04-01 12:00:00', '2025-04-23 12:00:00','BNEW', 'AVAI', 59, 7, 'https://panasonicjp.scene7.com/is/image/panasonicjp/DB-RM3M-C?fmt=png-alpha', 5, 7, 'OPEN', 800000, NULL, TRUE),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'LG Smart TV 55 Inch', '4K UHD Smart TV with Dolby Vision and AI sound.', '2025-02-03 12:00:00', '2025-05-03 12:00:00','GOOD', 'AVAI', 35, 2, 'https://www.lg.com/content/dam/channel/wcms/vn/images/tivi/55uq7550psf_atv_eavh_vn_c/gallery/DZ-01.jpg', 5, 8, 'DESI', 12000000, 1, TRUE),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'TOTO Smart Toilet Seat', 'Heated seat with bidet and self-cleaning function.', '2025-03-28 12:00:00', '2025-04-09 12:00:00', 'FAIR', 'EXPI', 79, 8, 'https://assets.totousa.com/MS922CUMFG/images/MS922CUMFG_01_2000_2000.webp', 6, 9, 'DESI', 5000000, 2, FALSE),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Bosch Kitchen Mixer', 'Stand mixer with multiple speed settings.', '2025-04-08 12:00:00', '2025-04-22 12:00:00','LNEW', 'AVAI', 7, 9, 'https://i.ytimg.com/vi/6_uLJQ14LKg/sddefault.jpg', 6, 10, 'DESI', 3500000, 3, FALSE),
    ('ACTIVE', 'admin', NOW(), NULL, NULL, 0, 'Eufy Video Doorbell', 'Smart doorbell with 2K resolution and motion detection.', '2025-04-18 12:00:00', '2025-05-18 12:00:00','EXCE', 'AVAI', 49, 5, 'https://shopdocla.vn/wp-content/uploads/2023/10/z4797756818217_42bf1646bfebde0bf05707ddaec05c2a.jpg', 7, 12, 'DESI', 2500000, 4, FALSE);

INSERT INTO public."ITEM_METHODEXCHANGES" ("ITEM_ITEM_ID", "METHOD_EXCHANGE") VALUES
                                                                                  (1, 'PICK'),
                                                                                  (1, 'MEET'),
                                                                                  (2, 'DELI'),
                                                                                  (3, 'MEET'),
                                                                                  (3, 'PICK'),
                                                                                  (4, 'PICK'),
                                                                                  (5, 'DELI'),
                                                                                  (5, 'MEET'),
                                                                                  (6, 'MEET'),
                                                                                  (7, 'DELI'),
                                                                                  (7, 'PICK'),
                                                                                  (8, 'PICK'),
                                                                                  (9, 'MEET'),
                                                                                  (9, 'DELI'),
                                                                                  (10, 'DELI'),
                                                                                  (10, 'PICK');

INSERT INTO public."SUBSCRIPTION_PLAN"
("STATUS_ENTITY", "USR_LOG_I", "DTE_LOG_I", "DTE_LOG_U", "USR_LOG_U", "VERSION", "DESCRIPTION", "DURATION", "IMAGE_URL", "NAME", "NUMBER_OF_FREE_EXTENSION", "PRICE", "TYPE_SUBSCRIPTION_PLAN")
VALUES
    ('ACTIVE', 'ngoccuong', '2025-03-24 22:14:05.890', '2025-03-25 00:03:43.882', 'ngoccuong', 5, 'This premium plan will last 1 month', 1, 'abc', 'Premium plan 1 month', 1, 299000, 'PREM'),
    ('ACTIVE', 'ngoccuong', '2025-03-25 00:01:01.318', '2025-03-25 00:01:01.318', NULL, 0, 'This premium plan will last 6 months', 6, 'abc', 'Premium plan 6 months', 5, 1499000,'PREM'),
    ('ACTIVE', 'ngoccuong', '2025-03-25 00:04:31.695', '2025-03-25 00:05:27.811', 'ngoccuong', 2, 'This premium plan will last 12 months', 12, 'abc', 'Premium plan 12 months', 10, 2999000, 'PREM'),
    ('ACTIVE', 'ngoccuong', '2025-03-25 00:20:36.583', '2025-03-25 00:20:36.583', NULL, 0, 'This plan will extend item for 2 more weeks', 0.5, 'abc', 'Extension plan 2 weeks', 0, 39000, 'IEXT');
