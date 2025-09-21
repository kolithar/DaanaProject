-- =====================================================
-- Dana Donation Platform - Sample Data INSERT Queries
-- =====================================================

-- Insert Categories with images
INSERT INTO category (id, name, description, image_url, status, created, updated) VALUES
(1, 'Animal Welfare', 'Programs focused on animal care, rescue, and welfare initiatives', 'https://resources.daana.lk/daana/animal-category.webp', 'ACTIVE', NOW(), NOW()),
(2, 'Education', 'Educational programs and initiatives for children and communities', 'https://resources.daana.lk/daana/education-category.webp', 'ACTIVE', NOW(), NOW()),
(3, 'Healthcare', 'Medical and healthcare support programs', 'https://resources.daana.lk/daana/healthcare-category.webp', 'ACTIVE', NOW(), NOW()),
(4, 'Environment', 'Environmental protection and conservation programs', 'https://resources.daana.lk/daana/environment-category.webp', 'ACTIVE', NOW(), NOW());

-- Insert SubCategories
INSERT INTO sub_category (id, name, description, status, created, updated, category_id) VALUES
-- Animal Welfare subcategories
(1, 'Pet Rescue', 'Rescue and rehabilitation of abandoned pets', 'ACTIVE', NOW(), NOW(), 1),
(2, 'Wildlife Conservation', 'Protection and conservation of wildlife', 'ACTIVE', NOW(), NOW(), 1),
(3, 'Animal Shelter Support', 'Support for animal shelters and care facilities', 'ACTIVE', NOW(), NOW(), 1),
-- Education subcategories
(4, 'School Supplies', 'Providing educational materials and supplies to schools', 'ACTIVE', NOW(), NOW(), 2),
(5, 'Scholarship Programs', 'Financial support for students in need', 'ACTIVE', NOW(), NOW(), 2),
(6, 'Digital Learning', 'Technology and digital learning initiatives', 'ACTIVE', NOW(), NOW(), 2),
-- Healthcare subcategories
(7, 'Medical Equipment', 'Providing medical equipment to healthcare facilities', 'ACTIVE', NOW(), NOW(), 3),
(8, 'Emergency Medical Aid', 'Emergency medical assistance and support', 'ACTIVE', NOW(), NOW(), 3),
(9, 'Mental Health Support', 'Mental health awareness and support programs', 'ACTIVE', NOW(), NOW(), 3),
-- Environment subcategories
(10, 'Tree Planting', 'Reforestation and tree planting initiatives', 'ACTIVE', NOW(), NOW(), 4),
(11, 'Waste Management', 'Waste reduction and management programs', 'ACTIVE', NOW(), NOW(), 4),
(12, 'Renewable Energy', 'Clean energy and sustainability projects', 'ACTIVE', NOW(), NOW(), 4);

-- Insert Charities
INSERT INTO charity (id, execution_type, email, password_hash, name, website, description, logo_url, mobile_number, nic_number_or_registration_number, contact_person_name, contact_person_mobile, contact_person_email, status, is_deleted, created, updated, account_verify_status) VALUES
(1, 'ORGANIZATION', 'animalcare@example.com', '$2a$10$NvOPxE/WHEvxAu2jhrbK3eoJ0dqmeumncWGZzta/hV1i64gaCay3i', 'Animal Care Foundation', 'https://animalcare.org', 'Dedicated to animal welfare and rescue operations', 'https://resources.daana.lk/charity-logos/animal-care-foundation.png', 771234567, 'REG123456', 'John Smith', 771234567, 'john@animalcare.org', 'ACTIVE', false, NOW(), NOW(), true),
(2, 'ORGANIZATION', 'education@example.com', '$2a$10$NvOPxE/WHEvxAu2jhrbK3eoJ0dqmeumncWGZzta/hV1i64gaCay3i', 'Education for All Foundation', 'https://educationforall.org', 'Providing quality education to underprivileged children', 'https://resources.daana.lk/charity-logos/education-for-all.png', 771234568, 'REG123457', 'Sarah Williams', 771234568, 'sarah@educationforall.org', 'ACTIVE', false, NOW(), NOW(), true),
(3, 'ORGANIZATION', 'healthcare@example.com', '$2a$10$NvOPxE/WHEvxAu2jhrbK3eoJ0dqmeumncWGZzta/hV1i64gaCay3i', 'Health Care Initiative', 'https://healthcareinitiative.org', 'Improving healthcare access in rural communities', 'https://resources.daana.lk/charity-logos/healthcare-initiative.png', 771234569, 'REG123458', 'Dr. Michael Brown', 771234569, 'michael@healthcareinitiative.org', 'ACTIVE', false, NOW(), NOW(), true),
(4, 'ORGANIZATION', 'environment@example.com', '$2a$10$NvOPxE/WHEvxAu2jhrbK3eoJ0dqmeumncWGZzta/hV1i64gaCay3i', 'Green Earth Society', 'https://greenearth.org', 'Environmental protection and sustainability initiatives', 'https://resources.daana.lk/charity-logos/green-earth-society.png', 771234570, 'REG123459', 'Emma Green', 771234570, 'emma@greenearth.org', 'ACTIVE', false, NOW(), NOW(), true);

-- Insert CharityCategory relationships
INSERT INTO charity_category (id, status, registered_moderator_username, updated, deleted, charity_id, category_id) VALUES
(1, 'ACTIVE', 'admin', NOW(), NULL, 1, 1),
(2, 'ACTIVE', 'admin', NOW(), NULL, 2, 2),
(3, 'ACTIVE', 'admin', NOW(), NULL, 3, 3),
(4, 'ACTIVE', 'admin', NOW(), NULL, 4, 4);

-- Insert Campaigns with thumbnail images
INSERT INTO campaigns (id, url_name, program_name, title, description, contact_person_email, contact_person_mobile, contact_person_name, deleted, program_location, target_donation_amount, raised, program_image, program_video, start_date, end_date, created, updated, status, charity_id, sub_category_id, related_document1, related_document2, related_document3) VALUES
-- Animal Care Foundation campaigns
(1, 'pet-rescue-emergency', 'Emergency Pet Rescue Fund', 'Help Save Abandoned Pets', 'This program focuses on rescuing abandoned and injured pets from the streets and providing them with immediate medical care, food, and shelter. Your donations help us save lives and give these animals a second chance.', 'rescue@animalcare.org', 771234567, 'Sarah Johnson', false, 'Colombo, Sri Lanka', 500000.00, 125000.00, 'https://resources.daana.lk/daana/youtube thumbnail.webp', 'https://youtube.com/watch?v=sample1', '2024-01-01 00:00:00', '2024-12-31 23:59:59', NOW(), NOW(), 'ACTIVE', 1, 1, 'https://resources.daana.lk/documents/emergency-rescue-protocol.pdf', 'https://resources.daana.lk/documents/medical-guidelines.pdf', 'https://resources.daana.lk/documents/pet-care-manual.pdf'),

(2, 'wildlife-conservation', 'Wildlife Protection Initiative', 'Protect Our Wildlife Heritage', 'Support our efforts to protect endangered wildlife species in Sri Lanka. This program includes habitat preservation, anti-poaching measures, and wildlife rehabilitation centers.', 'wildlife@animalcare.org', 771234567, 'Dr. Priya Fernando', false, 'National Parks, Sri Lanka', 1000000.00, 300000.00, 'https://resources.daana.lk/daana/youtube%20thumbnail-3.webp', 'https://youtube.com/watch?v=sample2', '2024-02-01 00:00:00', '2024-11-30 23:59:59', NOW(), NOW(), 'ACTIVE', 1, 2, 'https://resources.daana.lk/documents/wildlife-conservation-plan.pdf', 'https://resources.daana.lk/documents/habitat-protection.pdf', 'https://resources.daana.lk/documents/anti-poaching-strategy.pdf'),

(3, 'shelter-support', 'Animal Shelter Support Program', 'Support Local Animal Shelters', 'Help us provide essential supplies, medical equipment, and infrastructure improvements to local animal shelters across the country. Your support ensures better care for rescued animals.', 'shelter@animalcare.org', 771234567, 'Michael Raj', false, 'Multiple Locations, Sri Lanka', 750000.00, 200000.00, 'https://resources.daana.lk/daana/youtube%20thumbnail-1.webp', 'https://youtube.com/watch?v=sample3', '2024-03-01 00:00:00', '2024-10-31 23:59:59', NOW(), NOW(), 'ACTIVE', 1, 3, 'https://resources.daana.lk/documents/shelter-infrastructure.pdf', 'https://resources.daana.lk/documents/supply-schedule.pdf', 'https://resources.daana.lk/documents/equipment-requirements.pdf'),

-- Education for All Foundation campaigns
(4, 'school-supplies-drive', 'School Supplies for Rural Children', 'Equip Every Child with Learning Tools', 'Help us provide essential school supplies including books, stationery, and educational materials to children in rural areas who lack access to basic learning resources.', 'supplies@educationforall.org', 771234568, 'Lisa Chen', false, 'Rural Areas, Sri Lanka', 300000.00, 75000.00, 'https://resources.daana.lk/daana/education-thumbnail-1.webp', 'https://youtube.com/watch?v=education1', '2024-04-01 00:00:00', '2024-12-31 23:59:59', NOW(), NOW(), 'ACTIVE', 2, 4, 'https://resources.daana.lk/documents/school-supplies-plan.pdf', 'https://resources.daana.lk/documents/distribution-schedule.pdf', 'https://resources.daana.lk/documents/impact-assessment.pdf'),

(5, 'scholarship-program', 'Merit-Based Scholarship Fund', 'Support Bright Students in Need', 'Provide financial assistance to academically excellent students from low-income families to continue their education and achieve their dreams.', 'scholarships@educationforall.org', 771234568, 'David Kumar', false, 'Nationwide, Sri Lanka', 800000.00, 200000.00, 'https://resources.daana.lk/daana/education-thumbnail-2.webp', 'https://youtube.com/watch?v=education2', '2024-05-01 00:00:00', '2024-11-30 23:59:59', NOW(), NOW(), 'ACTIVE', 2, 5, 'https://resources.daana.lk/documents/scholarship-criteria.pdf', 'https://resources.daana.lk/documents/application-process.pdf', 'https://resources.daana.lk/documents/student-profiles.pdf'),

-- Health Care Initiative campaigns
(6, 'medical-equipment', 'Rural Hospital Equipment Fund', 'Modern Medical Equipment for Rural Hospitals', 'Help us equip rural hospitals and clinics with essential medical equipment to provide better healthcare services to underserved communities.', 'equipment@healthcareinitiative.org', 771234569, 'Dr. Raj Patel', false, 'Rural Hospitals, Sri Lanka', 1200000.00, 400000.00, 'https://resources.daana.lk/daana/healthcare-thumbnail-1.webp', 'https://youtube.com/watch?v=healthcare1', '2024-06-01 00:00:00', '2024-12-31 23:59:59', NOW(), NOW(), 'ACTIVE', 3, 7, 'https://resources.daana.lk/documents/equipment-requirements.pdf', 'https://resources.daana.lk/documents/hospital-list.pdf', 'https://resources.daana.lk/documents/installation-plan.pdf'),

(7, 'emergency-medical', 'Emergency Medical Response Fund', 'Rapid Response Medical Aid', 'Support our emergency medical response team that provides immediate medical assistance during natural disasters and emergencies.', 'emergency@healthcareinitiative.org', 771234569, 'Dr. Maria Silva', false, 'Disaster-Prone Areas, Sri Lanka', 600000.00, 150000.00, 'https://resources.daana.lk/daana/healthcare-thumbnail-2.webp', 'https://youtube.com/watch?v=healthcare2', '2024-07-01 00:00:00', '2024-10-31 23:59:59', NOW(), NOW(), 'ACTIVE', 3, 8, 'https://resources.daana.lk/documents/emergency-protocol.pdf', 'https://resources.daana.lk/documents/response-team.pdf', 'https://resources.daana.lk/documents/equipment-checklist.pdf'),

-- Green Earth Society campaigns
(8, 'tree-planting', 'Million Trees Initiative', 'Plant Trees for a Greener Future', 'Join us in planting one million trees across Sri Lanka to combat climate change, restore ecosystems, and create a sustainable environment for future generations.', 'trees@greenearth.org', 771234570, 'Alex Thompson', false, 'Forests and Urban Areas, Sri Lanka', 400000.00, 100000.00, 'https://resources.daana.lk/daana/environment-thumbnail-1.webp', 'https://youtube.com/watch?v=environment1', '2024-08-01 00:00:00', '2024-12-31 23:59:59', NOW(), NOW(), 'ACTIVE', 4, 10, 'https://resources.daana.lk/documents/tree-planting-plan.pdf', 'https://resources.daana.lk/documents/species-selection.pdf', 'https://resources.daana.lk/documents/maintenance-schedule.pdf'),

(9, 'waste-management', 'Community Waste Reduction Program', 'Zero Waste Communities', 'Help us implement comprehensive waste management systems in communities to reduce environmental pollution and promote recycling and composting.', 'waste@greenearth.org', 771234570, 'Sophie Green', false, 'Urban Communities, Sri Lanka', 350000.00, 87500.00, 'https://resources.daana.lk/daana/environment-thumbnail-2.webp', 'https://youtube.com/watch?v=environment2', '2024-09-01 00:00:00', '2024-11-30 23:59:59', NOW(), NOW(), 'ACTIVE', 4, 11, 'https://resources.daana.lk/documents/waste-management-strategy.pdf', 'https://resources.daana.lk/documents/community-training.pdf', 'https://resources.daana.lk/documents/recycling-guidelines.pdf');


-- Insert sample donations for campaigns
INSERT INTO donation (id, actual_donation_amount, net_donation_amount, service_charge, is_anonymous_donation, comments, status, payment_method, payment_slip_url, payment_reference_number, created, updated, campaigns_id, donation_package_id, registered_donor_id) VALUES
-- Animal Care Foundation donations
(1, 5000.00, 4750.00, 5.0, false, 'Great cause! Happy to help the animals.', 'ACTIVE', 'CARD', 'https://resources.daana.lk/payment-slips/payment-001.pdf', 'PAY-001-2024', NOW(), NOW(), 1, NULL, NULL),
(2, 10000.00, 9500.00, 5.0, true, 'Keep up the good work!', 'ACTIVE', 'BANK_TRANSFER', 'https://resources.daana.lk/payment-slips/payment-002.pdf', 'PAY-002-2024', NOW(), NOW(), 1, NULL, NULL),
(3, 2500.00, 2375.00, 5.0, false, 'Every little bit helps for wildlife conservation.', 'ACTIVE', 'CARD', 'https://resources.daana.lk/payment-slips/payment-003.pdf', 'PAY-003-2024', NOW(), NOW(), 2, NULL, NULL),
(4, 15000.00, 14250.00, 5.0, false, 'Supporting shelter infrastructure improvements.', 'ACTIVE', 'BANK_TRANSFER', 'https://resources.daana.lk/payment-slips/payment-004.pdf', 'PAY-004-2024', NOW(), NOW(), 3, NULL, NULL),
(5, 7500.00, 7125.00, 5.0, true, 'Anonymous donation for pet rescue.', 'ACTIVE', 'CARD', 'https://resources.daana.lk/payment-slips/payment-005.pdf', 'PAY-005-2024', NOW(), NOW(), 1, NULL, NULL),
-- Education for All Foundation donations
(6, 3000.00, 2850.00, 5.0, false, 'Education is the key to a better future.', 'ACTIVE', 'CARD', 'https://resources.daana.lk/payment-slips/payment-006.pdf', 'PAY-006-2024', NOW(), NOW(), 4, NULL, NULL),
(7, 8000.00, 7600.00, 5.0, false, 'Supporting bright students in need.', 'ACTIVE', 'BANK_TRANSFER', 'https://resources.daana.lk/payment-slips/payment-007.pdf', 'PAY-007-2024', NOW(), NOW(), 5, NULL, NULL),
-- Health Care Initiative donations
(8, 12000.00, 11400.00, 5.0, false, 'Healthcare should be accessible to all.', 'ACTIVE', 'CARD', 'https://resources.daana.lk/payment-slips/payment-008.pdf', 'PAY-008-2024', NOW(), NOW(), 6, NULL, NULL),
(9, 5000.00, 4750.00, 5.0, true, 'Supporting emergency medical response.', 'ACTIVE', 'BANK_TRANSFER', 'https://resources.daana.lk/payment-slips/payment-009.pdf', 'PAY-009-2024', NOW(), NOW(), 7, NULL, NULL),
-- Green Earth Society donations
(10, 4000.00, 3800.00, 5.0, false, 'Planting trees for our future generations.', 'ACTIVE', 'CARD', 'https://resources.daana.lk/payment-slips/payment-010.pdf', 'PAY-010-2024', NOW(), NOW(), 8, NULL, NULL),
(11, 2000.00, 1900.00, 5.0, false, 'Reducing waste for a cleaner environment.', 'ACTIVE', 'CARD', 'https://resources.daana.lk/payment-slips/payment-011.pdf', 'PAY-011-2024', NOW(), NOW(), 9, NULL, NULL);

-- =====================================================
-- End of Sample Data
-- =====================================================
