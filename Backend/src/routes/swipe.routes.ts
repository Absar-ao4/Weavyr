import { Hono } from "hono";
import { PrismaClient } from "@prisma/client/edge";
import { withAccelerate } from "@prisma/extension-accelerate";
import { authMiddleware } from "../middleware/auth.middleware";
import { swipeSchema } from "../validators/swipe.validator";

export const swipeRouter = new Hono<{
    Bindings: {
        ACCELERATE_URL: string;
        JWT_SECRET: string;
    },
    Variables: {
        userId: number;
    }
}>();

swipeRouter.post("/", authMiddleware, async (c) => {
    const prisma = new PrismaClient({
        datasourceUrl: c.env.ACCELERATE_URL,
      }).$extends(withAccelerate());
    
      const swiperId = Number(c.get("userId")); // ID of the user performing the swipe

      const body=await c.req.json();

      const parsed=swipeSchema.safeParse(body);
        if (!parsed.success) {
            c.status(400);
            return c.json({ error: "Invalid input" });
        }
      const {targetUserId, action}=parsed.data;
      
      // Prevent users from swiping on themselves
      if(swiperId === targetUserId) {
        c.status(400);
        return c.json({ error: "You cannot swipe on yourself" });
      }

      try{
        await prisma.swipe.upsert({
            where: {
                swiperId_swipedUserId: {
                    swiperId: swiperId,
                    swipedUserId: targetUserId,
                }
            },
            update: {
                action : action
            },
            create: {
                swiperId: swiperId,
                swipedUserId: targetUserId,
                action: action
            }
        });
        let isMatch = false;

        if(action === "LIKE") {
            const reciprocalSwipe = await prisma.swipe.findUnique({
                where: {
                    swiperId_swipedUserId: {
                        swiperId: targetUserId,
                        swipedUserId: swiperId,
                    }
                }
            });
            if(reciprocalSwipe && reciprocalSwipe.action === "LIKE") {
                isMatch = true;
                const user1Id= Math.min(swiperId, targetUserId);
                const user2Id= Math.max(swiperId, targetUserId);

                await prisma.collaboration.upsert({
                    where: {
                        user1Id_user2Id :{user1Id, user2Id}
                    },
                    update: {},
                    create: {
                        user1Id,
                        user2Id,
                        status: 'ACTIVE'
                    }
                });
            }
        }
        else if (action === "REJECT") {
    // Break the match if it existed
    const user1Id = Math.min(swiperId, targetUserId);
    const user2Id = Math.max(swiperId, targetUserId);

    // Using updateMany handles the case where the collaboration doesn't exist
    await prisma.collaboration.updateMany({
        where: { user1Id, user2Id },
        data: { status: 'ENDED' } 
    });
}
        return c.json({ message: "Swipe recorded successfully", isMatch });
    }
    catch (e) {
        c.status(500);
        return c.json({ error: "Failed to record swipe" });
    }
});

// Get pending connection requests (people who liked you, but you haven't swiped on yet)
swipeRouter.get("/requests", authMiddleware, async (c) => {
    const prisma = new PrismaClient({
        datasourceUrl: c.env.ACCELERATE_URL,
    }).$extends(withAccelerate());

    const userId = Number(c.get("userId"));

    try {
        const incomingRequests = await prisma.swipe.findMany({
            where: {
                // 1. They swiped on ME
                swipedUserId: userId,
                // 2. The action was a LIKE
                action: "LIKE",
                // 3. Look at the person who swiped (the 'swiper')...
                // Make sure they have NO swipes received from ME.
                swiper: {
                    swipesReceived: {
                        none: {
                            swiperId: userId
                        }
                    }
                }
            },
            select: {
                swiper: { 
                    select: {
                        id: true,
                        name: true,
                        username: true,
                        profilePhoto: true,
                        field: true,
                        organization: true,
                    }
                }
            },
            orderBy: {
                createdAt: "desc" 
            }
        });

        const formattedRequests = incomingRequests.map((req: any) => req.swiper);
        return c.json({ requests: formattedRequests });

    } catch (e) {
        c.status(500);
        return c.json({ error: "Failed to fetch connection requests" });
    }
});

// Get sent requests (people you have LIKEd)
swipeRouter.get("/sent", authMiddleware, async (c) => {
    const prisma = new PrismaClient({
        datasourceUrl: c.env.ACCELERATE_URL,
      }).$extends(withAccelerate());

    const userId = Number(c.get("userId"));

    try {
        const sentSwipes = await prisma.swipe.findMany({
            where: {
                swiperId: userId,
                action: "LIKE",
            },
            // Expand the select to grab ALL safe user profile fields
            select: {
                swipedUser: { 
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
                        linkedin: true,
                        googlescholar: true,
                        roles: true,
                        hIndex: true,
                        clusterId: true,
                        createdAt: true
                    }
                }
            },
            orderBy: {
                createdAt: "desc" 
            }
        });

        // Clean up the response to just send an array of user profiles
        const formattedSent = sentSwipes.map((req: any) => req.swipedUser);

        return c.json({ sent: formattedSent });

    } catch (e) {
        c.status(500);
        return c.json({ error: "Failed to fetch sent requests" });
    }
});

// Get rejected profiles (everyone you have REJECTed)
swipeRouter.get("/rejected", authMiddleware, async (c) => {
    const prisma = new PrismaClient({
        datasourceUrl: c.env.ACCELERATE_URL,
    }).$extends(withAccelerate());

    const userId = Number(c.get("userId"));

    try {
        const rejectedSwipes = await prisma.swipe.findMany({
            where: {
                swiperId: userId,
                action: "REJECT", // Fetches ALL your rejects
            },
            // Expand the select to grab ALL safe user profile fields
            select: {
                swipedUser: { 
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
                        linkedin: true,
                        googlescholar: true,
                        roles: true,
                        hIndex: true,
                        clusterId: true,
                        createdAt: true
                    }
                }
            },
            orderBy: {
                createdAt: "desc" 
            }
        });

        const formattedRejected = rejectedSwipes.map((req: any) => req.swipedUser);
        return c.json({ rejected: formattedRejected });

    } catch (e) {
        c.status(500);
        return c.json({ error: "Failed to fetch rejected profiles" });
    }
});