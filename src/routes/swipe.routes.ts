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
    
      const swiperId=c.get("userId"); // ID of the user performing the swipe

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
        return c.json({ message: "Swipe recorded successfully", isMatch });
    }
    catch (e) {
        c.status(500);
        return c.json({ error: "Failed to record swipe" });
    }
});

