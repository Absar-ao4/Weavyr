import {z} from "zod";

export const swipeSchema = z.object({
    targetUserId: z.number(),
    action : z.enum(["LIKE", "REJECT"]),
});

export type SwipeSchema = z.infer<typeof swipeSchema>;