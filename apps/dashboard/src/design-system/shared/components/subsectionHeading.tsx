interface SubSectionHeadingProps {
    title: string;
    description: string;
}

export default function SubSectionHeading({
    title,
    description,
}: Readonly<SubSectionHeadingProps>) {
    return (
        <div>
            <h2 className="text-2xl font-bold tracking-tight mb-2">{title}</h2>
            <p className="text-muted-foreground ">{description}</p>
        </div>
    );
}
