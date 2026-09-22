import { expect, test } from "vitest";
import { render, screen } from "@testing-library/react";
import LandingPage from "@/features/landingPage/components/landingPage";

test("renders get started link", () => {
    render(<LandingPage />);
    expect(screen.getByRole("link", { name: "Get Started" })).toBeDefined();
});
