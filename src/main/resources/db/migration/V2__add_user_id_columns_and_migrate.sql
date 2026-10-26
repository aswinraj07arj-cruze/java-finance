-- V2: Upgrade schema for Spring Security multi-user isolation and migrate legacy records

ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(150);
ALTER TABLE users ADD COLUMN IF NOT EXISTS full_name VARCHAR(150) DEFAULT 'User' NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255) DEFAULT '' NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(50) DEFAULT 'USER' NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;

-- Allow nullable username for users registering with email
ALTER TABLE users ALTER COLUMN username DROP NOT NULL;

-- Existing users rows with username only get a non-null full_name and a unique email
UPDATE users SET email = username || '@legacy.local' WHERE (email IS NULL OR email = '') AND username IS NOT NULL;
UPDATE users SET full_name = username WHERE (full_name IS NULL OR full_name = 'User') AND username IS NOT NULL;

-- Ensure all existing legacy users are disabled (enabled=false) until explicit claim / setup
UPDATE users SET enabled = FALSE;

-- 2. Add user_id foreign key columns to financial entities
ALTER TABLE incomes ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(id);
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(id);
ALTER TABLE budgets ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(id);
ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(id);

-- 3. If no users exist (fresh database install), seed default system user
INSERT INTO users (full_name, email, username, password_hash, role, enabled, created_at)
SELECT 'Aswin', 'aswin@example.com', 'aswin@example.com', '$2a$10$7zB3cT6qE4Wl5u8YxK9Z1eO4rJ5t7u9v1w3x5y7z9A1b3c5d7e9fG', 'USER', false, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users);

-- 4. Associate legacy orphaned rows with the primary legacy user (earliest created user)
UPDATE incomes SET user_id = (SELECT id FROM users ORDER BY id ASC LIMIT 1) WHERE user_id IS NULL;
UPDATE expenses SET user_id = (SELECT id FROM users ORDER BY id ASC LIMIT 1) WHERE user_id IS NULL;
UPDATE budgets SET user_id = (SELECT id FROM users ORDER BY id ASC LIMIT 1) WHERE user_id IS NULL;
UPDATE subscriptions SET user_id = (SELECT id FROM users ORDER BY id ASC LIMIT 1) WHERE user_id IS NULL;

-- 5. Deduplicate any budget records per (user_id, category) prior to applying unique constraint
DELETE FROM budgets WHERE id NOT IN (
    SELECT MIN(id) FROM budgets GROUP BY user_id, category
);

-- 6. Enforce NOT NULL constraints on user_id columns
ALTER TABLE incomes ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE expenses ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE budgets ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE subscriptions ALTER COLUMN user_id SET NOT NULL;

-- 7. Add unique constraint for (user_id, category) on budgets
CREATE UNIQUE INDEX IF NOT EXISTS uq_budgets_user_category ON budgets (user_id, category);
