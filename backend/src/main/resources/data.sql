-- Users
INSERT INTO users (id, name, email, role) VALUES
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Alice Johnson', 'alice@st6.com', 'MANAGER'),
    ('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Bob Smith', 'bob@st6.com', 'EMPLOYEE'),
    ('c3d4e5f6-a7b8-9012-cdef-123456789012', 'Carol Davis', 'carol@st6.com', 'EMPLOYEE'),
    ('d4e5f6a7-b8c9-0123-defa-234567890123', 'Dan Wilson', 'dan@st6.com', 'EMPLOYEE');

-- RCDO Goal Hierarchy
-- Rally Cries
INSERT INTO goal_nodes (id, title, level, parent_id) VALUES
    ('10000000-0000-0000-0000-000000000001', 'Become the #1 platform in our market by Q3', 'RALLY_CRY', NULL),
    ('10000000-0000-0000-0000-000000000002', 'Achieve operational excellence across all teams', 'RALLY_CRY', NULL);

-- Defining Objectives under Rally Cry 1
INSERT INTO goal_nodes (id, title, level, parent_id) VALUES
    ('20000000-0000-0000-0000-000000000001', 'Launch 3 enterprise features by end of quarter', 'DEFINING_OBJECTIVE', '10000000-0000-0000-0000-000000000001'),
    ('20000000-0000-0000-0000-000000000002', 'Grow user base by 40%', 'DEFINING_OBJECTIVE', '10000000-0000-0000-0000-000000000001');

-- Defining Objectives under Rally Cry 2
INSERT INTO goal_nodes (id, title, level, parent_id) VALUES
    ('20000000-0000-0000-0000-000000000003', 'Reduce incident response time to under 15 minutes', 'DEFINING_OBJECTIVE', '10000000-0000-0000-0000-000000000002'),
    ('20000000-0000-0000-0000-000000000004', 'Implement CI/CD best practices across all repos', 'DEFINING_OBJECTIVE', '10000000-0000-0000-0000-000000000002');

-- Outcomes under Defining Objective 1
INSERT INTO goal_nodes (id, title, level, parent_id) VALUES
    ('30000000-0000-0000-0000-000000000001', 'Payment integration shipped and live', 'OUTCOME', '20000000-0000-0000-0000-000000000001'),
    ('30000000-0000-0000-0000-000000000002', 'SSO integration complete', 'OUTCOME', '20000000-0000-0000-0000-000000000001'),
    ('30000000-0000-0000-0000-000000000003', 'Advanced reporting dashboard launched', 'OUTCOME', '20000000-0000-0000-0000-000000000001');

-- Outcomes under Defining Objective 2
INSERT INTO goal_nodes (id, title, level, parent_id) VALUES
    ('30000000-0000-0000-0000-000000000004', 'Referral program live and tracking conversions', 'OUTCOME', '20000000-0000-0000-0000-000000000002'),
    ('30000000-0000-0000-0000-000000000005', 'Marketing site redesign complete', 'OUTCOME', '20000000-0000-0000-0000-000000000002');

-- Outcomes under Defining Objective 3
INSERT INTO goal_nodes (id, title, level, parent_id) VALUES
    ('30000000-0000-0000-0000-000000000006', 'Automated alerting pipeline deployed', 'OUTCOME', '20000000-0000-0000-0000-000000000003'),
    ('30000000-0000-0000-0000-000000000007', 'Runbook coverage at 100% for critical services', 'OUTCOME', '20000000-0000-0000-0000-000000000003');

-- Outcomes under Defining Objective 4
INSERT INTO goal_nodes (id, title, level, parent_id) VALUES
    ('30000000-0000-0000-0000-000000000008', 'All repos have automated test pipelines', 'OUTCOME', '20000000-0000-0000-0000-000000000004'),
    ('30000000-0000-0000-0000-000000000009', 'Deploy frequency increased to daily', 'OUTCOME', '20000000-0000-0000-0000-000000000004');

-- Current week
INSERT INTO weeks (id, start_date, end_date) VALUES
    ('a0000000-0000-0000-0000-000000000001', '2026-03-30', '2026-04-03');
