import { Hono } from "hono";
import { PrismaClient } from "@prisma/client/edge";
import { withAccelerate } from "@prisma/extension-accelerate";
import { authMiddleware } from "../middleware/auth.middleware";
import { updateUserSchema } from "../validators/user.validator";
import { ca, he } from "zod/locales";

export const userRouter = new Hono<{
  Bindings: {
    ACCELERATE_URL: string;
    JWT_SECRET: string;
    HF_CLUSTERING_URL: string;
    HF_API_KEY: string;
  },
  Variables: {
    userId: number;
  }
}>();

// Update user profile (protected)
userRouter.put("/updateprofile", authMiddleware,async (c) => {
    const prisma = new PrismaClient({
        datasourceUrl: c.env.ACCELERATE_URL,
      }).$extends(withAccelerate());

    const userId =Number( c.get("userId"));

    const body = await c.req.json();

    const parsed= updateUserSchema.safeParse(body);

    if (!parsed.success) {
      c.status(400);
      return c.json({ error: "Invalid input" });
    }

let assignedClusterId = null;

    if (parsed.data.interests && parsed.data.interests.length > 0) {
        const controller = new AbortController();
        // Give it 15 seconds instead of 8, HF spaces can sometimes take a moment to respond
        const timeoutId = setTimeout(() => controller.abort(), 15000); 
        
        console.log("Checking ML URL:", c.env.HF_CLUSTERING_URL);

        try {
            const mlResponse = await fetch(c.env.HF_CLUSTERING_URL, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    // Hugging Face expects standard Bearer token authorization, not X-API-KEY
                    "Authorization": `Bearer ${c.env.HF_API_KEY}` 
                },
                body: JSON.stringify({
                    // Join the interests array into a comma-separated string just like Postman
                    concepts: parsed.data.interests.join(", ") 
                }),
                signal: controller.signal
            });
            
            clearTimeout(timeoutId);
            
            if (mlResponse.ok) {
                // Parse the response
                const mlData = await mlResponse.json() as any;
                
                // Python APIs often use snake_case (cluster_id), while JS uses camelCase (clusterId). 
                // This handles both just in case!
                assignedClusterId = mlData.clusterId || mlData.cluster_id || mlData.cluster; 
                console.log("Assigned Cluster ID:", assignedClusterId);
            } else {
                console.error("ML API error:", mlResponse.statusText);
                c.status(502);
                return c.json({error: "Failed to generate recommendation cluster"});
            }
        } catch (error: any) {
            clearTimeout(timeoutId);
            if (error.name === 'AbortError') {
                console.error("ML API timeout: Model is likely warming up.");
                c.status(504);
                return c.json({error: "Recommendation model is warming up. Please try again in 10 seconds."});
            }

            console.error("Failed to fetch clustering ID:", error);
            c.status(500); 
            return c.json({ error: "Failed to connect to recommendation service." });
        }
    }

    try{
        await prisma.user.update({
  where: { id: userId },
  data: {
    name: parsed.data.name,
    field: parsed.data.field,
    organization: parsed.data.organization,
    experienceYears: parsed.data.experienceYears,
    profilePhoto: parsed.data.profilePhoto,
    education: parsed.data.education,
    numberOfPapers: parsed.data.numberOfPapers,
    totalCitations: parsed.data.citationCount,
    linkedin:parsed.data.linkedin,
    googlescholar:parsed.data.googlescholar,
    roles : parsed.data.roles,
    clusterId: assignedClusterId ?? undefined, // Update cluster ID based on interests
    achievements: parsed.data.achievements
      ? {
          deleteMany: {},// Clears old achievements so we don't get duplicates
          create: parsed.data.achievements.map((a: any) => ({
            title: a.title,
            description: a.description,
            year: a.year,
          })),
        }
      : undefined,

   interests: parsed.data.interests
    ? {
        deleteMany: {},// Clears old interests so we don't get duplicates
        create: parsed.data.interests.map((interestName: string) => ({
            interest: {
                connectOrCreate: {
                    where: { name: interestName },
                    create: { name: interestName },
                },
            },
        })),
     }
    : undefined,
    papersAuthored: parsed.data.papersAuthored ? {
    deleteMany: {},
    create: parsed.data.papersAuthored.map((p: any) => ({
        authorOrder: p.authorOrder,
        paper: {
            connectOrCreate: {
                where: { paperUrl: p.paperUrl },
                create: {
                    title: p.title,
                    abstract: p.abstract,
                    journal: p.journal,
                    publicationYear: p.publicationYear,
                    paperUrl: p.paperUrl,
                }
            }
        }
    })),
} : undefined,
  }
});
    return c.json({message:"Profile updated successfully"});
    } 
    catch (e) {
        c.status(500);
        return c.json({ error: "Failed to update profile" });
    }
});


// Get user profile (protected)
userRouter.get("/fetchprofile", authMiddleware, async (c) => {
    const prisma = new PrismaClient({
        datasourceUrl: c.env.ACCELERATE_URL,
      }).$extends(withAccelerate());

      const userid=Number(c.get("userId"));
      try {
    const user = await prisma.user.findUnique({
      where: {
        id: userid,
      },
      select: {
        id: true,
        name: true,
        username: true,
        email: true,
        profilePhoto: true,
        field: true,
        organization: true,
        education: true,
        experienceYears: true,
        numberOfPapers: true,
        totalCitations: true,
        linkedin:true,
        googlescholar:true,
        clusterId:true,
        roles: true,
        
        // 1. Fetch Achievements (Direct relation)
        achievements: {
                    select: {
                        id: true,
                        title: true,
                        description: true,
                        year: true
                    }
                },

                // 2. Fetch Interests (Nested through UserInterest)
                interests: {
                    select: {
                        interest: {
                            select: {
                                id: true,
                                name: true
                            }
                        }
                    }
                },

                // 3. Fetch Papers (Nested through PaperAuthor)
                papersAuthored: {
                    select: {
                        authorOrder: true,
                        paper: {
                            select: {
                                id: true,
                                title: true,
                                abstract: true,
                                journal: true,
                                publicationYear: true,
                                paperUrl: true,
                                citationCount: true
                            }
                        }
                    }
                }
            }
        });
    if (!user) {
      c.status(404);
      return c.json({ error: "User not found" });
    }
    const formattedUser = {
            ...user,
            
            // Map over the join tables to extract the actual data
            interests: user.interests.map((ui: any) => ui.interest.name),
            
            papersAuthored: user.papersAuthored.map((pa: any) => ({
                id: pa.paper.id,
                title: pa.paper.title,
                abstract: pa.paper.abstract,
                journal: pa.paper.journal,
                publicationYear: pa.paper.publicationYear,
                paperUrl: pa.paper.paperUrl,
                citationCount: pa.paper.citationCount,
                authorOrder: pa.authorOrder
            }))
        };

        return c.json({ user: formattedUser });
  } 
  catch (e) {
    c.status(500);
    return c.json({ error: "Failed to fetch user profile" });
  }
});

// Get any user's profile by ID (public)
userRouter.get("/:id", async (c) => {
    const prisma = new PrismaClient({
        datasourceUrl: c.env.ACCELERATE_URL,
      }).$extends(withAccelerate());

      const targetUserId = Number(c.req.param("id"));
        if(!Number.isInteger((targetUserId))) {
        c.status(400);
        return c.json({ error: "Invalid user ID" });
      }

      try{
        const user = await prisma.user.findUnique({
            where: {
                id: targetUserId,
            },
            select: {
                id: true,
                name: true,
                username: true,
                profilePhoto: true,
                field: true,
                organization: true,
                education: true,
                experienceYears: true,
                numberOfPapers: true,
                totalCitations: true,
                linkedin:true,
                roles: true,
                googlescholar:true,
                achievements: {
                    select: {
                        id: true,
                        title: true,
                        description: true,
                        year: true
                    }
                },
                interests: {
                    select: {
                        interest: {
                            select: {
                                id: true,
                                name: true
                            }
                        }
                    }
                },
                papersAuthored: {
                    select: {
                        authorOrder: true,
                        paper: {
                            select: {
                                id: true,
                                title: true,
                                abstract: true,
                                journal: true,
                                publicationYear: true,
                                paperUrl: true,
                                citationCount: true
                            }
                        }
                    }
                }
                ,
                badges: {
                    select: {
                        badge: {
                            select: {
                                id: true,
                                name: true,
                                description: true,
                            }
                        }
                    }
                }
            }
        });
        if (!user) {
            c.status(404);
            return c.json({ error: "User not found" });
        }

        const formattedUser = {
            ...user,
            interests: user.interests.map((ui: any) => ui.interest.name),
            papersAuthored: user.papersAuthored.map((pa: any) => ({
                id: pa.paper.id,
                title: pa.paper.title,
                abstract: pa.paper.abstract,
                journal: pa.paper.journal,
                publicationYear: pa.paper.publicationYear,
                paperUrl: pa.paper.paperUrl,
                citationCount: pa.paper.citationCount,
                authorOrder: pa.authorOrder
            })),
            badges: user.badges.map((ub: any) => ({
                id: ub.badge.id,
                name: ub.badge.name,
                description: ub.badge.description,
            }))
        };
        return c.json({ user: formattedUser });
      }
      catch (e) {
        c.status(500);
        return c.json({ error: "Failed to fetch user profile" });
      }
});

