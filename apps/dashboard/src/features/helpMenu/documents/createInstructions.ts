export interface Ins{
    id : string;
    name : string;
    description : string;
    details : string[];
}

export const creatingIns = (
    forInstructions : Omit<Ins, 'id'>[]
) : Ins[] => {
    return forInstructions.map((ins,forIndex) => ({
        ...ins, id : `ins${forIndex+1}`
    }));
};