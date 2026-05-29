'use client';
import React, { useState } from 'react';
import { Button } from '@/components/atoms/button';
import { Input } from '@/components/atoms/input';
import { Label } from '@/components/atoms/label';

interface PropsForStepOne{
  onNext: (data: {
    accessKeyId: string;
    secretAccessKey: string;
    region: string;
  }) => void;
}

export default function StepOne({ onNext }: Readonly<PropsForStepOne>){
  const [formData, setFormData] = useState({
    accessKeyId: '', secretAccessKey: '', region: 'region',
  });

  const handleSubmit = (forHandlingSubmit: React.SubmitEvent<HTMLFormElement>) => {
    forHandlingSubmit.preventDefault();
    onNext(formData);
  };

  const regions = [
    'regions'
  ];

  return(
    <div className="min-h-screen bg-background flex items-center justify-center p-8">
      <div className="w-full max-w-2xl bg-card rounded-lg shadow-none p-8">
        <div className="pb-6">
          <div className="flex items-center gap-2 mb-4">
            <div className="w-2 h-2 rounded-full bg-primary" />

            <span className="text-sm font-medium text-muted-foreground/70">
              STEP 1 OF 3
            </span>

          </div>

          <h2 className="text-2xl font-semibold tracking-tight text-foreground">
            Link your AWS account
          </h2>

          <p className="mt-2 text-muted-foreground/70">
            Enter your AWS credentials to connect your account.
          </p>

        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">

            <Label htmlFor="accessKeyId" className="text-foreground text-sm font-medium">
              Access key ID
            </Label>

            <Input
              id="accessKeyId"
              type="text"
              placeholder="EXAMPLE"
              value={formData.accessKeyId}
              onChange={(e) => setFormData({ ...formData, accessKeyId: e.target.value })}
              className="bg-background border-border rounded-md px-4 py-3 text-foreground placeholder:text-muted-foreground/40 focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition-all w-full"
              required
            />

          </div>

          <div className="space-y-2">

            <Label htmlFor="secretAccessKey" className="text-foreground text-sm font-medium">
              Secret access key
            </Label>

            <Input
              id="secretAccessKey"
              type="password"
              placeholder="••••••••••••••••••••••••"
              value={formData.secretAccessKey}
              onChange={(e) => setFormData({ ...formData, secretAccessKey: e.target.value })}
              className="bg-background border-border rounded-md px-4 py-3 text-foreground placeholder:text-muted-foreground/40 focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition-all w-full"
              required
            />

          </div>

          <div className="space-y-2">

            <Label htmlFor="region" className="text-foreground text-sm font-medium">
              Region
            </Label>

            <select
              id="region"
              value={formData.region}
              onChange={(e) => setFormData({ ...formData, region: e.target.value })}
              className="w-full bg-background border-border rounded-md px-4 py-3 text-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition-all cursor-pointer"
            >

              {regions.map((region) => (
                <option key={region} value={region} className="bg-card">
                  {region}
                </option>
              ))}

            </select>
          </div>

          <div className="flex justify-end pt-4">

            <Button
              type="submit"
              className="bg-primary hover:bg-accent hover:text-accent-foreground text-primary-foreground px-6 py-2 rounded-md transition-all duration-200 font-medium"
            >
              Next
            </Button>
            
          </div>
        </form>
      </div>
    </div>
  );
}