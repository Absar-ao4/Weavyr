import { Hono } from "hono";
import { PrismaClient } from "@prisma/client/edge";
import { withAccelerate } from "@prisma/extension-accelerate";
import { authMiddleware } from "../middleware/auth.middleware";

export const paperLikeRouter = new Hono<{
    Bindings: {
        ACCELERATE_URL: string,
        JWT_SECRET: string;
    },
    Variables: {
        userId: number;
    }
}>();

// Toggle a like on a paper (If liked -> unlike. If not liked -> like)
paperLikeRouter.post("/:paperId/toggle-like", authMiddleware, async (c) => {
    const prisma = new PrismaClient({
        datasourceUrl: c.env.ACCELERATE_URL,
    }).$extends(withAccelerate());

    const userId = Number(c.get("userId"));
    const paperId = Number(c.req.param("paperId"));

    if (!Number.isInteger(paperId)) {
        c.status(400);
        return c.json({ error: "Invalid paper ID" });
    }

    try {
        // Check if the like already exists
        const existingLike = await prisma.paperLike.findUnique({
            where: {
                userId_paperId: {
                    userId: userId,
                    paperId: paperId,
                }
            }
        });

        if (existingLike) {
            // UNLIKE: Remove the record
            await prisma.paperLike.delete({
                where: { id: existingLike.id }
            });
            return c.json({ message: "Paper unliked successfully", isLiked: false });
        } else {
            // LIKE: Create the record
            await prisma.paperLike.create({
                data: {
                    userId: userId,
                    paperId: paperId,
                }
            });
            return c.json({ message: "Paper liked successfully", isLiked: true });
        }
    } catch (e) {
        console.error("Error toggling paper like:", e);
        c.status(500);
        return c.json({ error: "Failed to process like action" });
    }
});

// Get all papers the authenticated user has liked
paperLikeRouter.get("/my-likes", authMiddleware, async (c) => {
    const prisma = new PrismaClient({
        datasourceUrl: c.env.ACCELERATE_URL,
    }).$extends(withAccelerate());

    const userId = Number(c.get("userId"));

    try {
        const likes = await prisma.paperLike.findMany({
            where: { userId: userId },
            include: {
                paper: {
                    include: {
                        authors: {
                            include: {
                                // CHANGED: Set to true to get ALL user details
                                user: true 
                            }
                        }
                    }
                }
            },
            orderBy: {
                createdAt: "desc" 
            }
        });

        // Forced any to bypass the Edge Client type erasure
        const formattedLikedPapers = likes.map((like: any) => {
            return like.paper;
        });
        
        return c.json({ likedPapers: formattedLikedPapers });

    } catch (e) {
        console.error("Failed to fetch liked papers:", e);
        c.status(500);
        return c.json({ error: "Failed to fetch liked papers" });
    }
});