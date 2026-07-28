interface SubSectionHeadingProps {
    title: string;
    description: string;
}

export default function SubSectionHeading({
    title,
    description,
}: Readonly<SubSectionHeadingProps>) {
    return (
        <div className=" flex flex-col items-start justify-between">
            <h2 className="text-2xl font-bold tracking-tight">{title}</h2>
            <p className="text-muted-foreground ">{description}</p>
        </div>
    );
}
