import { Hono } from "hono";
import { PrismaClient } from "@prisma/client/edge";
import { withAccelerate } from "@prisma/extension-accelerate";
import { authMiddleware } from "../middleware/auth.middleware";

export const bookmarkRouter = new Hono<{
    Bindings: {
        ACCELERATE_URL: string,
        JWT_SECRET: string;
    },
    Variables: {
        userId: number;
    }
}>();


// Get all bookmarked profiles for the authenticated user
bookmarkRouter.get("/", authMiddleware, async (c) => {
    const prisma = new PrismaClient({
        datasourceUrl: c.env.ACCELERATE_URL,
      }).$extends(withAccelerate());

    const userId = c.get("userId");

    try{
        const [profilebookmarks] = await Promise.all([
            prisma.profileBookmark.findMany({
                where: { userId: userId },
                // Switched from 'include' to 'select' to grab exactly the fields we want
                select: {
                    bookmarkedUser: {
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
                        },
                    },
                },
            }),
        ]);
        return c.json({ profileBookmarks: profilebookmarks.map((b) => b.bookmarkedUser) });
    }
    catch (e) {
        c.status(500);
        return c.json({ error: "Failed to fetch bookmarked profiles" });
    }
});

// Bookmark a profile
bookmarkRouter.post("/profiles/:userId", authMiddleware, async (c) => {
    const prisma = new PrismaClient({
        datasourceUrl: c.env.ACCELERATE_URL,
      }).$extends(withAccelerate());

    const userId = c.get("userId");
    const targetUserId = Number(c.req.param("userId"));

    if(!Number.isInteger(targetUserId)) {
        c.status(400);
        return c.json({ error: "Invalid user ID" });
    }

    if(userId === targetUserId) {
        c.status(400);
        return c.json({ error: "You cannot bookmark yourself" });
    }

    try{
        await prisma.profileBookmark.create({
            data: {
                userId: userId,
                bookmarkedUserId: targetUserId,
            },
        });
        return c.json({ message: "Profile bookmarked successfully" });
    }
    catch (e : any) {
        if(e.code=="P2002") {
            c.status(400);
            return c.json({ error: "Profile is already bookmarked" });
        }
        c.status(500);
        return c.json({ error: "Failed to bookmark profile" });
    }
});

// Unbookmark a profile
bookmarkRouter.delete("/profiles/:userId", authMiddleware, async (c) => {
    const prisma = new PrismaClient({
        datasourceUrl: c.env.ACCELERATE_URL,
      }).$extends(withAccelerate());

    const userId = c.get("userId");
    const targetUserId = Number(c.req.param("userId"));

    if(!Number.isInteger(targetUserId)) {
        c.status(400);
        return c.json({ error: "Invalid user ID" });
    }

    try{
        await prisma.profileBookmark.delete({
            where: {
                userId_bookmarkedUserId: {
                    userId: userId,
                    bookmarkedUserId: targetUserId,
                },
            },
        });
        return c.json({ message: "Profile removed successfully" });
    }
    catch (e : any) {
        if(e.code=="P2025") {
            c.status(400);
            return c.json({ error: "Bookmark does not exist" });
        }
        c.status(500);
        return c.json({ error: "Failed to remove bookmark" });
    }
});