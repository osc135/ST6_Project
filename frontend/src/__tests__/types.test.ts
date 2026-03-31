import { Priority, CommitStatus, UserRole, GoalLevel, PRIORITY_LABELS } from "../types";

describe("Type enums and constants", () => {
  it("Priority enum has 4 values", () => {
    expect(Object.values(Priority)).toHaveLength(4);
    expect(Priority.KING).toBe("KING");
    expect(Priority.QUEEN).toBe("QUEEN");
    expect(Priority.KNIGHT).toBe("KNIGHT");
    expect(Priority.PAWN).toBe("PAWN");
  });

  it("CommitStatus enum has 5 values", () => {
    expect(Object.values(CommitStatus)).toHaveLength(5);
    expect(CommitStatus.DRAFT).toBe("DRAFT");
    expect(CommitStatus.LOCKED).toBe("LOCKED");
    expect(CommitStatus.RECONCILING).toBe("RECONCILING");
    expect(CommitStatus.RECONCILED).toBe("RECONCILED");
    expect(CommitStatus.CARRY_FORWARD).toBe("CARRY_FORWARD");
  });

  it("UserRole enum has 2 values", () => {
    expect(Object.values(UserRole)).toHaveLength(2);
  });

  it("GoalLevel enum has 3 values", () => {
    expect(Object.values(GoalLevel)).toHaveLength(3);
  });

  it("PRIORITY_LABELS maps all priorities", () => {
    expect(PRIORITY_LABELS[Priority.KING].label).toBe("King");
    expect(PRIORITY_LABELS[Priority.KING].meaning).toBe("Critical");
    expect(PRIORITY_LABELS[Priority.QUEEN].label).toBe("Queen");
    expect(PRIORITY_LABELS[Priority.KNIGHT].label).toBe("Knight");
    expect(PRIORITY_LABELS[Priority.PAWN].label).toBe("Pawn");
    expect(PRIORITY_LABELS[Priority.PAWN].meaning).toBe("Low");
  });
});
