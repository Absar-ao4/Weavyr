import { Context, Next } from "hono";
import { verify } from "hono/jwt";

type Bindings = {
  JWT_SECRET: string;
};

// Define variables stored in 'c.set'
type Variables = {
  userId: number;
};

type JwtPayload = {
  userId: number;
};

export const authMiddleware = async (c: Context<{ Bindings: Bindings, Variables: Variables }>, next: Next)=> {
  const authHeader = c.req.header("Authorization");

  if(!authHeader || !authHeader.startsWith("Bearer ")) {
    c.status(401);
    return c.json({ error: "Unauthorized" });
  }
  const token=authHeader.split(" ")[1];
  try {
    const payload = await verify(token, c.env.JWT_SECRET, "HS256") as JwtPayload;
    if (!payload.userId) {
      c.status(401);
      return c.json({ error: "Invalid token" });
    }
    c.set("userId", payload.userId);
    await next();
  } catch (e) {
    c.status(401);
    return c.json({ error: "Invalid token" });
  }
}

