import { z } from "zod";

export const updateUserSchema = z.object({
  name: z.string().min(2),
  education: z.string(),
  field: z.string().optional(),
  organization: z.string().optional(),
  experienceYears: z.number().optional(),
  profilePhoto: z.string().optional(),
  achievements: z.array(
    z.object({
      title: z.string(),
      description: z.string().optional(),
      year: z.number().optional(),
    })
  ).optional(),
    interests: z.array(z.string().min(2)).min(1),
  numberOfPapers: z.number().optional(),
  citationCount: z.number().optional(),
  linkedin:z.string().url().optional(),
  googlescholar:z.string().url().optional(),
  papersAuthored: z.array(
    z.object({
      title: z.string(),
      abstract: z.string().optional(),
      journal: z.string().optional(),
      publicationYear: z.number().optional(),
      paperUrl: z.string().url("Must be a valid URL"), // Made required and validated as a URL
      authorOrder: z.number().optional(),
    })
  ).optional(),
});

export type UpdateUserSchema = z.infer<typeof updateUserSchema>;