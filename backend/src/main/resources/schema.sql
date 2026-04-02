CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL DEFAULT '',
    role VARCHAR(20) NOT NULL CHECK (role IN ('EMPLOYEE', 'MANAGER')),
    manager_id UUID,
    FOREIGN KEY (manager_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS weeks (
    id UUID PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS goal_nodes (
    id UUID PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    level VARCHAR(30) NOT NULL CHECK (level IN ('RALLY_CRY', 'DEFINING_OBJECTIVE', 'OUTCOME')),
    parent_id UUID,
    FOREIGN KEY (parent_id) REFERENCES goal_nodes(id)
);

CREATE TABLE IF NOT EXISTS weekly_commits (
    id UUID PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    priority VARCHAR(10) NOT NULL CHECK (priority IN ('KING', 'QUEEN', 'KNIGHT', 'PAWN')),
    goal_id UUID,
    custom_goal_text VARCHAR(500),
    is_custom_goal BOOLEAN NOT NULL DEFAULT FALSE,
    owner_id UUID NOT NULL,
    week_id UUID NOT NULL,
    carried_over_from UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'LOCKED', 'RECONCILING', 'RECONCILED', 'CARRY_FORWARD')),
    FOREIGN KEY (goal_id) REFERENCES goal_nodes(id),
    FOREIGN KEY (owner_id) REFERENCES users(id),
    FOREIGN KEY (week_id) REFERENCES weeks(id),
    FOREIGN KEY (carried_over_from) REFERENCES weekly_commits(id)
);

CREATE TABLE IF NOT EXISTS reconciliation_entries (
    id UUID PRIMARY KEY,
    commit_id UUID NOT NULL,
    done BOOLEAN NOT NULL,
    explanation VARCHAR(1000),
    FOREIGN KEY (commit_id) REFERENCES weekly_commits(id)
);
