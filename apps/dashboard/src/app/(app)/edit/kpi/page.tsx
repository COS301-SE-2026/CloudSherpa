import { Button } from "@/components/atoms/button";
import { Card, CardTitle } from "@/components/atoms/card";

export default function EditKpiPage() {
    return (
        <main className="flex flex-1 flex-col gap-6 p-6 lg:p-8 w-full mx-auto">
            <div className="flex flex-row gap-6">
                <h1 className="text-2xl">KPI Configuration</h1>
                <Button variant={"default"}>Save KPI</Button>
                <Button variant={"secondary"}>Cancel</Button>
            </div>
            <div className="grid grid-cols-[2fr_1fr] gap-4 h-full">
                <Card className="p-6"></Card>
                <Card className="p-6">
                    <CardTitle>Preview</CardTitle>
                </Card>
            </div>
        </main>
    );
}
