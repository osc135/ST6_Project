Weekly Commit Module
Organization: ST6   |   Status: Active   |   Category: AI-Accelerated
Roles: All Roles   |   Stack: TypeScript (strict) · Java 21 · SQL

1. Overview
The Weekly Commit Module is a production-ready micro-frontend that replaces 15-Five as ST6's weekly planning tool. The core problem with 15-Five is that employees write weekly plans in a vacuum — there is no structural connection between what individuals plan to do and what the organization is trying to achieve. Managers cannot identify misalignment until it is too late.
This module solves that by building a structured weekly check-in system with three core moments: planning at the start of the week, reconciliation at the end of the week, and a manager dashboard that provides real-time visibility into team alignment.

2. Problem Statement
The organization currently uses 15-Five for weekly planning, but it has no structural connection between individual weekly commitments and organizational strategic goals. Managers lack visibility into how team members' weekly work maps to Rally Cries, Defining Objectives, and Outcomes, making it impossible to identify misalignment until it is too late.

3. Goals & Assumptions
3.1 Goals
	•	Replace 15-Five entirely for the weekly planning workflow
	•	Force every task to be connected to an organizational goal
	•	Give managers real-time visibility into team alignment
	•	Support the full weekly lifecycle from planning through reconciliation
3.2 Key Assumptions
The following assumptions were made in the absence of direct stakeholder access and should be validated:
Assumption
Rationale
RCDO hierarchy is pre-loaded by an admin
Employees pick from a dropdown; no employee-facing CRUD for goals
Single organization — no multi-tenancy
Spec refers to 'the organization' (singular)
One week = Monday to Friday
Standard work week; system auto-creates week containers
No in-week status tracking
Reconciliation is batched at end of week per spec
Carry Forward = auto-copy to next week
Undone tasks appear in next week's Carried Over section automatically
Chess layer = 4 priority tiers
King, Queen, Knight, Pawn — maps to High/Medium-High/Medium/Low
Managers are also employees
They have all employee views plus the Team Dashboard


4. RCDO Goal Hierarchy
RCDO stands for Rally Cries, Defining Objectives, and Outcomes. This is ST6's internal goal cascade framework, analogous to OKRs. The hierarchy flows from company-level ambition down to individual weekly tasks:
Rally Cry  →  Defining Objective  →  Outcome  →  Weekly Commit
Example:
Level
Example
Rally Cry
Become the #1 platform in our market by Q3
Defining Objective
Launch 3 enterprise features by end of quarter
Outcome
Payment integration shipped and live
Weekly Commit (employee task)
Wire up Stripe webhook handler

When entering a task, employees select from a dropdown of pre-loaded Outcomes. If no goal fits, they may type a custom reason — this is flagged visually on the manager dashboard as unaligned.

5. User Roles
Role
Access
Employee
My Week view, Reconcile view, soft nudge banner
Manager
All employee views + Team Dashboard


6. Core Features
6.1 My Week (Employee Dashboard)
The default landing page for all users. Shown every day of the work week.
Layout
	•	Two sections: This Week and Carried Over
	•	Add task button accessible from the dashboard (corner/floating)
	•	Soft nudge banner on Monday if last week has not been reconciled
Soft Nudge Banner
Displayed at the top of My Week on Monday morning if the prior week is unreconciled:
  ⚠  You haven't reconciled last week yet. Do it here →
The banner is dismissible. The employee is never hard-blocked from accessing My Week.
6.2 Task Card
Each task the employee enters has the following fields:
Field
Type
Notes
Task Name
Text input
Required. What the employee is doing.
Company Goal
Combo box (dropdown + free text)
Required. Links to RCDO Outcome. Free text is allowed but flagged.
Priority
Select (4 options)
King · Queen · Knight · Pawn (High to Low)
Status
System-managed
Driven by weekly lifecycle state machine

Delivery date is not a field — all tasks are implicitly due by end of the current week.
6.3 Goal Selection (Combo Box)
	•	Primary: dropdown of pre-loaded org goals (RCDO Outcomes)
	•	Secondary: employee may type a custom reason if no goal fits
	•	Custom reasons are stored with an is_custom_goal flag = true
	•	Custom goals are visually flagged on the manager dashboard as unaligned
6.4 Priority / Chess Layer
Chess Piece
Priority Level
Meaning
King
Critical
Must be done this week. Highest importance.
Queen
High
Strong priority. Should be done this week.
Knight
Medium
Important but flexible if needed.
Pawn
Low
Nice to have. Carries forward without concern.

6.5 Weekly Lifecycle State Machine
Each task moves through the following states:
State
When
Employee Can Edit?
DRAFT
Task created, week not yet locked
Yes — full edit
LOCKED
Week is locked (e.g. Monday end of day)
No
RECONCILING
End of week, reconciliation open
Only Done/Not Done + explanation
RECONCILED
Employee has submitted reconciliation
No
CARRY_FORWARD
Task marked not done, copied to next week
Yes — starts new DRAFT

6.6 Reconciliation View (Friday)
Accessible via the Reconcile tab in the navigation. Shows all tasks from the prior week.
Per-task interaction
	•	Two buttons: Done and Not Done
	•	Employee clicks one button per task
	•	If Not Done is selected, a text box appears requiring a one-sentence explanation
	•	Done tasks are marked RECONCILED and closed out
	•	Not Done tasks are marked CARRY_FORWARD and automatically appear in next week's Carried Over section
Submission
	•	Employee submits the full reconciliation when all tasks have been addressed
	•	State transitions to RECONCILED for the week
6.7 Manager Team Dashboard
Accessible only to users with the Manager role. Shows the full team's weekly commitments and alignment status.
Team Roll-Up View
Column
Description
Team Member
Employee name
Tasks This Week
Count of tasks in current week
Alignment Status
Visual indicator: green / yellow / red

Alignment Indicators
Indicator
Meaning
✅ Green — All aligned
All tasks linked to org goals via dropdown
⚠️ Yellow — Partially aligned
One or more tasks have custom/typed goals
🔴 Red — Unaligned
One or more tasks have no goal selected at all

Drill-Down View
Manager can click any team member to see their individual task cards, including the goal each task is linked to, the explanation for any unreconciled tasks, and alignment flags.

7. Navigation
Nav Item
Visible To
Description
My Week
All users
Default landing page. This week + Carried Over sections.
Reconcile
All users
End-of-week done/not done flow for prior week's tasks.
Team Dashboard
Managers only
Team roll-up with alignment indicators.


8. Data Model
The following entities are required to support the described functionality:
User
Field
Type
Notes
id
UUID
Primary key
name
VARCHAR
Display name
email
VARCHAR
Unique, used for auth
role
ENUM
EMPLOYEE or MANAGER

Week
Field
Type
Notes
id
UUID
Primary key
start_date
DATE
Monday of the week
end_date
DATE
Friday of the week

GoalNode (RCDO Hierarchy)
Field
Type
Notes
id
UUID
Primary key
title
VARCHAR
Goal description
level
ENUM
RALLY_CRY, DEFINING_OBJECTIVE, or OUTCOME
parent_id
UUID (FK)
References parent GoalNode. Null for Rally Cries.

WeeklyCommit (Task)
Field
Type
Notes
id
UUID
Primary key
name
VARCHAR
Task name as entered by employee
priority
ENUM
KING, QUEEN, KNIGHT, or PAWN
goal_id
UUID (FK, nullable)
References GoalNode (Outcome level preferred)
custom_goal_text
VARCHAR (nullable)
Free-text goal if no org goal selected
is_custom_goal
BOOLEAN
True if custom_goal_text is used instead of goal_id
owner_id
UUID (FK)
References User
week_id
UUID (FK)
References Week
carried_over_from
UUID (FK, nullable)
References prior WeeklyCommit if carried forward
status
ENUM
DRAFT, LOCKED, RECONCILING, RECONCILED, CARRY_FORWARD

ReconciliationEntry
Field
Type
Notes
id
UUID
Primary key
commit_id
UUID (FK)
References WeeklyCommit
done
BOOLEAN
True if employee marked Done
explanation
VARCHAR (nullable)
Required if done = false


9. Technical Requirements
Layer
Technology
Notes
Frontend
TypeScript (strict mode)
Micro-frontend module following PM remote pattern
Backend
Java 21
REST API serving frontend
Database
SQL
Relational schema as defined in Section 8
Integration
PA host app (Module Federation)
Module plugs into existing app shell


10. Out of Scope (v1)
	•	Admin CRUD for RCDO goal hierarchy — goals are pre-loaded
	•	Multi-tenancy / multi-organization support
	•	In-week task status updates — reconciliation is end-of-week only
	•	Notifications or email reminders (soft nudge banner only)
	•	Mobile-native application
	•	Historical analytics or reporting beyond current week

This PRD reflects decisions made based on reasonable assumptions in the absence of direct stakeholder access. All assumptions in Section 3.2 should be validated before development begins.
