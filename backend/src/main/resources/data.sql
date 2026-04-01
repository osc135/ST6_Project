-- Users (manager first, then employees with manager_id pointing to Alice)
INSERT INTO users (id, name, email, password, role, manager_id) VALUES
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Alice Johnson', 'alice@st6.com', 'password', 'MANAGER', NULL);
INSERT INTO users (id, name, email, password, role, manager_id) VALUES
    ('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Bob Smith', 'bob@st6.com', 'password', 'EMPLOYEE', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'),
    ('c3d4e5f6-a7b8-9012-cdef-123456789012', 'Carol Davis', 'carol@st6.com', 'password', 'EMPLOYEE', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'),
    ('d4e5f6a7-b8c9-0123-defa-234567890123', 'Dan Wilson', 'dan@st6.com', 'password', 'EMPLOYEE', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890');

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

-- Weeks
INSERT INTO weeks (id, start_date, end_date) VALUES
    ('a0000000-0000-0000-0000-000000000002', '2026-03-23', '2026-03-27'),
    ('a0000000-0000-0000-0000-000000000001', '2026-03-30', '2026-04-03');

-- =============================================
-- PRIOR WEEK TASKS (2026-03-23 to 2026-03-27)
-- =============================================

-- Alice (Manager) - prior week: 2 reconciled, 1 carried forward
INSERT INTO weekly_commits (id, name, priority, goal_id, custom_goal_text, is_custom_goal, owner_id, week_id, carried_over_from, status) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'Review Q3 roadmap with leadership', 'KING', '30000000-0000-0000-0000-000000000001', NULL, FALSE, 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'a0000000-0000-0000-0000-000000000002', NULL, 'RECONCILED'),
    ('c0000000-0000-0000-0000-000000000002', 'Finalize team OKR scoring', 'QUEEN', '30000000-0000-0000-0000-000000000003', NULL, FALSE, 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'a0000000-0000-0000-0000-000000000002', NULL, 'RECONCILED'),
    ('c0000000-0000-0000-0000-000000000003', 'Draft hiring plan for Q4', 'KNIGHT', NULL, 'Internal planning - no goal yet', TRUE, 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'a0000000-0000-0000-0000-000000000002', NULL, 'CARRY_FORWARD');

INSERT INTO reconciliation_entries (id, commit_id, done, explanation) VALUES
    ('e0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', TRUE, NULL),
    ('e0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000002', TRUE, NULL),
    ('e0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000003', FALSE, 'Waiting on headcount approval from finance');

-- Bob - prior week: 1 reconciled, 1 still LOCKED (unreconciled - triggers nudge)
INSERT INTO weekly_commits (id, name, priority, goal_id, custom_goal_text, is_custom_goal, owner_id, week_id, carried_over_from, status) VALUES
    ('c0000000-0000-0000-0000-000000000004', 'Set up Stripe webhook handler', 'KING', '30000000-0000-0000-0000-000000000001', NULL, FALSE, 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'a0000000-0000-0000-0000-000000000002', NULL, 'RECONCILED'),
    ('c0000000-0000-0000-0000-000000000005', 'Write payment integration tests', 'QUEEN', '30000000-0000-0000-0000-000000000001', NULL, FALSE, 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'a0000000-0000-0000-0000-000000000002', NULL, 'LOCKED');

INSERT INTO reconciliation_entries (id, commit_id, done, explanation) VALUES
    ('e0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000004', TRUE, NULL);

-- =============================================
-- CURRENT WEEK TASKS (2026-03-30 to 2026-04-03)
-- =============================================

-- Alice (Manager) - 3 tasks: 2 aligned, 1 custom goal (yellow alignment)
INSERT INTO weekly_commits (id, name, priority, goal_id, custom_goal_text, is_custom_goal, owner_id, week_id, carried_over_from, status) VALUES
    ('c0000000-0000-0000-0000-000000000010', 'Draft hiring plan for Q4', 'KNIGHT', NULL, 'Internal planning - no goal yet', TRUE, 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'a0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000003', 'DRAFT'),
    ('c0000000-0000-0000-0000-000000000011', 'Run sprint planning for payment team', 'KING', '30000000-0000-0000-0000-000000000001', NULL, FALSE, 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'a0000000-0000-0000-0000-000000000001', NULL, 'DRAFT'),
    ('c0000000-0000-0000-0000-000000000012', 'Review SSO architecture proposal', 'QUEEN', '30000000-0000-0000-0000-000000000002', NULL, FALSE, 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'a0000000-0000-0000-0000-000000000001', NULL, 'DRAFT');

-- Bob - 3 tasks: 2 aligned, 1 custom goal (yellow alignment)
INSERT INTO weekly_commits (id, name, priority, goal_id, custom_goal_text, is_custom_goal, owner_id, week_id, carried_over_from, status) VALUES
    ('c0000000-0000-0000-0000-000000000013', 'Implement Stripe checkout flow', 'KING', '30000000-0000-0000-0000-000000000001', NULL, FALSE, 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'a0000000-0000-0000-0000-000000000001', NULL, 'DRAFT'),
    ('c0000000-0000-0000-0000-000000000014', 'Add payment error handling', 'QUEEN', '30000000-0000-0000-0000-000000000001', NULL, FALSE, 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'a0000000-0000-0000-0000-000000000001', NULL, 'DRAFT'),
    ('c0000000-0000-0000-0000-000000000015', 'Fix flaky CI pipeline', 'PAWN', NULL, 'Tech debt cleanup', TRUE, 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'a0000000-0000-0000-0000-000000000001', NULL, 'DRAFT');

-- Carol - 3 tasks: all aligned to org goals (green alignment)
INSERT INTO weekly_commits (id, name, priority, goal_id, custom_goal_text, is_custom_goal, owner_id, week_id, carried_over_from, status) VALUES
    ('c0000000-0000-0000-0000-000000000016', 'Build referral tracking dashboard', 'KING', '30000000-0000-0000-0000-000000000004', NULL, FALSE, 'c3d4e5f6-a7b8-9012-cdef-123456789012', 'a0000000-0000-0000-0000-000000000001', NULL, 'DRAFT'),
    ('c0000000-0000-0000-0000-000000000017', 'Design email templates for referral invites', 'QUEEN', '30000000-0000-0000-0000-000000000004', NULL, FALSE, 'c3d4e5f6-a7b8-9012-cdef-123456789012', 'a0000000-0000-0000-0000-000000000001', NULL, 'DRAFT'),
    ('c0000000-0000-0000-0000-000000000018', 'Write referral API endpoint tests', 'KNIGHT', '30000000-0000-0000-0000-000000000004', NULL, FALSE, 'c3d4e5f6-a7b8-9012-cdef-123456789012', 'a0000000-0000-0000-0000-000000000001', NULL, 'DRAFT');

-- Dan - 3 tasks: 1 aligned, 1 custom, 1 with NO goal at all (red alignment)
INSERT INTO weekly_commits (id, name, priority, goal_id, custom_goal_text, is_custom_goal, owner_id, week_id, carried_over_from, status) VALUES
    ('c0000000-0000-0000-0000-000000000019', 'Set up alerting for production errors', 'KING', '30000000-0000-0000-0000-000000000006', NULL, FALSE, 'd4e5f6a7-b8c9-0123-defa-234567890123', 'a0000000-0000-0000-0000-000000000001', NULL, 'DRAFT'),
    ('c0000000-0000-0000-0000-000000000020', 'Update personal dev environment', 'PAWN', NULL, 'Personal productivity', TRUE, 'd4e5f6a7-b8c9-0123-defa-234567890123', 'a0000000-0000-0000-0000-000000000001', NULL, 'DRAFT'),
    ('c0000000-0000-0000-0000-000000000021', 'Attend company all-hands', 'PAWN', NULL, NULL, FALSE, 'd4e5f6a7-b8c9-0123-defa-234567890123', 'a0000000-0000-0000-0000-000000000001', NULL, 'DRAFT');
