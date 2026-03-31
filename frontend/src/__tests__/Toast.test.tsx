import React from "react";
import { render, screen, fireEvent, act } from "@testing-library/react";
import "@testing-library/jest-dom";
import { ToastProvider, useToast } from "../context/ToastContext";
import { ToastContainer } from "../components/ToastContainer";

// Helper that renders ToastContainer + buttons to trigger toasts
function TestHarness() {
  const { showError, showSuccess } = useToast();
  return (
    <>
      <button onClick={() => showError("Something went wrong")}>Trigger Error</button>
      <button onClick={() => showSuccess("Task saved")}>Trigger Success</button>
      <ToastContainer />
    </>
  );
}

function renderWithProvider() {
  return render(
    <ToastProvider>
      <TestHarness />
    </ToastProvider>
  );
}

describe("Toast system", () => {
  beforeEach(() => {
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it("renders nothing when there are no toasts", () => {
    renderWithProvider();
    expect(screen.queryByText("Something went wrong")).not.toBeInTheDocument();
    expect(screen.queryByText("Task saved")).not.toBeInTheDocument();
  });

  it("shows an error toast when showError is called", () => {
    renderWithProvider();
    fireEvent.click(screen.getByText("Trigger Error"));
    expect(screen.getByText("Something went wrong")).toBeInTheDocument();
  });

  it("shows a success toast when showSuccess is called", () => {
    renderWithProvider();
    fireEvent.click(screen.getByText("Trigger Success"));
    expect(screen.getByText("Task saved")).toBeInTheDocument();
  });

  it("applies toast-error class for error toasts", () => {
    renderWithProvider();
    fireEvent.click(screen.getByText("Trigger Error"));
    const toast = screen.getByText("Something went wrong").closest(".toast");
    expect(toast).toHaveClass("toast-error");
  });

  it("applies toast-success class for success toasts", () => {
    renderWithProvider();
    fireEvent.click(screen.getByText("Trigger Success"));
    const toast = screen.getByText("Task saved").closest(".toast");
    expect(toast).toHaveClass("toast-success");
  });

  it("dismisses a toast when the close button is clicked", () => {
    renderWithProvider();
    fireEvent.click(screen.getByText("Trigger Error"));
    expect(screen.getByText("Something went wrong")).toBeInTheDocument();

    const closeBtn = screen.getByText("×");
    fireEvent.click(closeBtn);
    expect(screen.queryByText("Something went wrong")).not.toBeInTheDocument();
  });

  it("auto-dismisses success toasts after 3 seconds", () => {
    renderWithProvider();
    fireEvent.click(screen.getByText("Trigger Success"));
    expect(screen.getByText("Task saved")).toBeInTheDocument();

    act(() => {
      jest.advanceTimersByTime(3000);
    });
    expect(screen.queryByText("Task saved")).not.toBeInTheDocument();
  });

  it("auto-dismisses error toasts after 6 seconds", () => {
    renderWithProvider();
    fireEvent.click(screen.getByText("Trigger Error"));
    expect(screen.getByText("Something went wrong")).toBeInTheDocument();

    act(() => {
      jest.advanceTimersByTime(3000);
    });
    // Still visible at 3s
    expect(screen.getByText("Something went wrong")).toBeInTheDocument();

    act(() => {
      jest.advanceTimersByTime(3000);
    });
    // Gone at 6s
    expect(screen.queryByText("Something went wrong")).not.toBeInTheDocument();
  });

  it("can show multiple toasts at once", () => {
    renderWithProvider();
    fireEvent.click(screen.getByText("Trigger Error"));
    fireEvent.click(screen.getByText("Trigger Success"));

    expect(screen.getByText("Something went wrong")).toBeInTheDocument();
    expect(screen.getByText("Task saved")).toBeInTheDocument();
  });

  it("dismissing one toast does not affect others", () => {
    renderWithProvider();
    fireEvent.click(screen.getByText("Trigger Error"));
    fireEvent.click(screen.getByText("Trigger Success"));

    // Dismiss only the error toast
    const closeBtns = screen.getAllByText("×");
    fireEvent.click(closeBtns[0]);

    expect(screen.queryByText("Something went wrong")).not.toBeInTheDocument();
    expect(screen.getByText("Task saved")).toBeInTheDocument();
  });

  it("returns null from ToastContainer when toast list is empty", () => {
    const { container } = renderWithProvider();
    expect(container.querySelector(".toast-container")).not.toBeInTheDocument();
  });

  it("throws if useToast is called outside ToastProvider", () => {
    function BadComponent() {
      useToast();
      return null;
    }
    // Suppress console.error for expected error
    const spy = jest.spyOn(console, "error").mockImplementation(() => {});
    expect(() => render(<BadComponent />)).toThrow(
      "useToast must be used within ToastProvider"
    );
    spy.mockRestore();
  });
});
