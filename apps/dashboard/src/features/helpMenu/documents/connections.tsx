"use client";

import {Breadcrumb, BreadcrumbList, BreadcrumbItem, BreadcrumbLink, BreadcrumbPage, BreadcrumbSeparator} from "@/components/atoms/breadcrumb";
import {Card, CardContent} from "@/components/atoms/card";
import {Plug} from "lucide-react";

/*
- this page give users all the info on how to add, remove and manage their conn
*/

interface Instructions{
  id : string;
  name : string;
  description : string;
  details : string[];
}

const INS : Instructions[] = [
  {id : "ins1", name : "Navigate to Connection Manager", description : "Access the Connection Manager from your dashboard",
   details : [
    "From your dashboard, locate the sidebar on the left",
    "Click on Connection Manager",
    "You will be redirected to the Connection Manager page where you can view and configure your cloud connections"
   ],
  },

  {id : "ins2", name : "Add an AWS cloud provider", description : "Follow the steps of the wizard to add your first cloud provider",
   details : [
    "On the Connection Manager page, click on add button located in the top right corner",
    "A wizard will appear",
    "Follow the steps of the wizard and you will be able to successfully add your first AWS connection"
   ],
  },

  {id : "ins3", name : "Enter your credentials", description : "Provide your credentials for the cloud provider",
    details : [
      "Enter your cloud providers credentials",
      "Ensure you have the required permissions"
    ],
  },

  {id : "ins4", name : "Select services and resources", description : "Choose which services and resources you would like CloudSherpa to monitor",
   details : [
    "Select the services you want to keep track of",
    "Choose the specific resources within each service you want to monitor"
   ],
  },

  {id : "ins5", name : "Manage your connections", description : "View and configure your connections",
   details : [
    "You can add or remove connections at any time from the Connection Manager",
    "You are able to rename your connections"
   ],
  },
];

export default function Connection(){
  return(
    <div className = "min-h-screen bg-background">
      <div className = "border-b border-border px-8 py-5">

        <Breadcrumb>
          <BreadcrumbList>
            <BreadcrumbItem>
              <BreadcrumbLink href = "/" className = "text-[13px] text-muted-foreground"> Dashboard </BreadcrumbLink>
            </BreadcrumbItem>

            <BreadcrumbSeparator/>

            <BreadcrumbItem>
              <BreadcrumbLink href = "/helpMenu/documentsAndTutorials" className = "text-[13px] text-muted-foreground"> Help Center </BreadcrumbLink>
            </BreadcrumbItem>

            <BreadcrumbSeparator/>

            <BreadcrumbItem>
              <BreadcrumbPage className = "text-[13px] font-medium text-foreground"> Connections </BreadcrumbPage>
            </BreadcrumbItem>
          </BreadcrumbList>
        </Breadcrumb>

      </div>
    

    <div className = "mx-auto max-w-[720px] px-6 pb-20 pt-12">
      <div className = "mb-10 flex items-center gap-4">
        <div className = "flex h-12 w-12 shrink-0 items-center justify-center rounded-lg bg-muted text-primary"> <Plug className = "h-6 w-6" strokeWidth = {1.75}/> </div>

        <div>
          <h1 className = "text-[24px] font-medium text-foreground"> Connecting your AWS cloud provider </h1>

          <p className = "mt-1 text-[14px] text-muted-foreground"> Follow these five steps to connect your AWS account </p>
        </div>

      </div>

      <div className = "flex flex-col gap-4">
        {INS.map((instruction,i) => {
          return(
            <Card key = {instruction.id} className = "gap-0 overflow-hidden border-border bg-muted/40 p-0">
              <CardContent className = "p-5">
                <div className = "flex items-start gap-4">
                  <div className = "flex flex-col items-center">
                    <span className = "flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-muted text-[12px] font-medium text-primary"> {i+1} </span>

                    {i < INS.length-1 && (
                      <span className = "mt-2 h-full w-px flex-1 bg-border"/>
                    )}

                  </div>

                  <div className = "min-w-0 flex-1 pb-1">
                    <h2 className = "text-[15px] font-medium text-foreground"> {instruction.name} </h2>

                    <p className = "mt-1 text-[13px] text-muted-foreground"> {instruction.description} </p>

                    <ul className = "mt-3 flex flex-col gap-2">
                      {instruction.details.map((detail) => (
                        <li key = {detail} className = "flex items-start gap-2 text-[12.5px] text-muted-foreground">
                          <span className = "mt-1.5 h-1 w-1 shrink-0 rounded-full bg-muted-foreground/60"/>

                          <span> {detail} </span>
                        </li>

                      ))}
                    </ul>
                  </div>

                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>
    </div>

  </div>
  );
}