import { Hono } from "hono";
import { PrismaClient } from "@prisma/client/edge";
import { withAccelerate } from "@prisma/extension-accelerate";
import { authMiddleware } from "../middleware/auth.middleware";
import { ca } from "zod/locales";

export const discoverRouter = new Hono<{
    Bindings: {
        ACCELERATE_URL: string;
        JWT_SECRET: string;
    },
    Variables: {
        userId: number;
    }
}>();

discoverRouter.get("/feed", authMiddleware, async (c) => {
    const prisma = new PrismaClient({
        datasourceUrl: c.env.ACCELERATE_URL,
      }).$extends(withAccelerate());

      const userId=c.get("userId"); // ID of the user requesting the feed

      try{
        const currentUser = await prisma.user.findUnique({
            where: { id: userId },
            select: { clusterId: true }
        });

        if(!currentUser?.clusterId){
            c.status(400);
            return c.json({ error: "Please complete your recommendation to get recommendations" });
        }

        const pastSwipes = await prisma.swipe.findMany({
            where: { swiperId: userId },
            select: { swipedUserId: true }
        });

        const excludedIds=pastSwipes.map((s:any)=>s.swipedUserId); // Exclude already swiped users and self
        excludedIds.push(userId);

        const recommendations = await prisma.user.findMany({
            where: {
                clusterId: currentUser.clusterId,
                id: { notIn: excludedIds }
            },
            take : 15,
            select: {
                id: true,
                name: true,
                username: true,
                profilePhoto: true,
                field: true,
                education: true,
                organization: true,
                experienceYears: true,
                numberOfPapers: true,
                totalCitations: true,
                interests: {
                    select: { interest: { select: { name: true } } }
                },
                achievements: {
                    select: { title: true, description: true }
                }
            }
        });

        const formattedRecs = recommendations.map((rec) => ({
            ...rec,
            interests: rec.interests.map((i) => i.interest.name)
        }));
        return c.json({ recommendations: formattedRecs });
      }
      catch(e){
        c.status(500);
        return c.json({ error: "An error occurred while fetching recommendations" });
      }
});