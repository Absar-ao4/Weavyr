import { Hono } from "hono";
import { PrismaClient } from "@prisma/client/edge";
import { withAccelerate } from "@prisma/extension-accelerate";
import { authMiddleware } from "../middleware/auth.middleware";

export const collaborationRouter = new Hono<{
    Bindings: {
        ACCELERATE_URL: string,
        JWT_SECRET: string;
    },
    Variables: {
        userId: number;
    }
}>();

collaborationRouter.get("/", authMiddleware, async (c) => {
    const prisma = new PrismaClient({
        datasourceUrl: c.env.ACCELERATE_URL,
      }).$extends(withAccelerate());

    const userId = c.get("userId");

    try{
        const collaborations = await prisma.collaboration.findMany({
            where: {
                OR: [{user1Id: userId}, {user2Id: userId}],
                status: "ACTIVE"
            },
            include:{
                user1: {
                    select: {
                        id: true,
                        name: true,
                        username: true,
                        profilePhoto: true,
                        field: true,
                        organization: true,
                },
            },
                user2: {
                    select: {
                        id: true,
                        name: true,
                        username: true,
                        profilePhoto: true,
                        field: true,
                        organization: true,
                    },
                },
            },
            orderBy: {
                createdAt: "desc",
             },
        });
        const formattedCollaborations = collaborations.map((collab) => {
            const otherUser = collab.user1Id === userId ? collab.user2Id : collab.user1Id;
            return {
                id: collab.id,
                user: otherUser,
                createdAt: collab.createdAt,
            };
        });
        return c.json({collaborations: formattedCollaborations});
    }
    catch (e) {
        c.status(500);
        return c.json({ error: "Failed to fetch collaborations" });
    }
});

collaborationRouter.get("/:collabId", authMiddleware, async (c) => {
    const prisma = new PrismaClient({
        datasourceUrl: c.env.ACCELERATE_URL,
      }).$extends(withAccelerate());

      const userId = c.get("userId");
      const collabId = Number(c.req.param("collabId"));

      if(!Number.isInteger(collabId)) {
        c.status(400);
        return c.json({ error: "Invalid collaboration ID" });
      }

      try{
        const collaboration = await prisma.collaboration.findUnique({
            where: { id: collabId },
            include:{
                user1: {
                    select: { id: true, name: true, username: true, profilePhoto: true, field: true, education: true, organization: true },
                },
                user2: {
                    select: { id: true, name: true, username: true, profilePhoto: true, field: true, education: true, organization: true },
                },
            },
        });
        if(!collaboration){
            c.status(404);
            return c.json({ error: "Collaboration not found" });
        }
        if(collaboration.user1Id !== userId && collaboration.user2Id !== userId) {
            c.status(403);
            return c.json({ error: "Unauthorized access to this collaboration" });
        }
        const partner = collaboration.user1Id === userId ? collaboration.user2 : collaboration.user1;
        return c.json({ collaboration: {
            id: collaboration.id,
            partner: partner,
            createdAt: collaboration.createdAt,
         }});
  }
    catch (e) {
        c.status(500);
        return c.json({ error: "Failed to fetch collaboration details" });
    }
});


collaborationRouter.delete("/:collabId", authMiddleware, async (c) => {
    const prisma = new PrismaClient({
        datasourceUrl: c.env.ACCELERATE_URL,
      }).$extends(withAccelerate());

    const userId = c.get("userId");
    const collabId = Number(c.req.param("collabId"));

    if(!Number.isInteger(collabId)) {
        c.status(400);
        return c.json({ error: "Invalid collaboration ID" });
    }
    
    try{
        const collaboration = await prisma.collaboration.findUnique({
            where: { id: collabId },
        });

        if(!collaboration){
            c.status(404);
            return c.json({ error: "Collaboration not found" });
        }

        if(collaboration.user1Id !== userId && collaboration.user2Id !== userId) {
            c.status(403);
            return c.json({ error: "Unauthorized access to this collaboration" });
        }

        await prisma.collaboration.update({
            where: { id: collabId },
            data :{ status: "ENDED" }
        });

        const targetUserId = collaboration.user1Id === userId ? collaboration.user2Id : collaboration.user1Id;

        await prisma.swipe.updateMany({
            where: {
            OR: [
                { swiperId: userId, swipedUserId: targetUserId },
                { swiperId: targetUserId, swipedUserId: userId }
            ]
        },
            data: {
                action: "REJECT"
            }
         });

        return c.json({ message: "Collaboration ended successfully" });
    }
    catch (e) {
        c.status(500);
        return c.json({ error: "Failed to end collaboration" });
    }
});