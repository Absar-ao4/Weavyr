import { Hono } from "hono";
import { PrismaClient } from "@prisma/client/edge";
import { withAccelerate } from "@prisma/extension-accelerate";
import { sign } from "hono/jwt";
import { loginSchema, signupSchema } from "../validators/auth.validator";
import bcrypt from "bcryptjs";


export const authRouter = new Hono<{
  Bindings: {
    ACCELERATE_URL: string;
    JWT_SECRET: string;
  }
}>();

// Signup route
authRouter.post("/signup", async (c) => {
  const prisma = new PrismaClient({
    datasourceUrl: c.env.ACCELERATE_URL,
  }).$extends(withAccelerate());

  const body = await c.req.json();

  const {success}=signupSchema.safeParse(body);
  
  if (!success) {
    c.status(400);
    return c.json({ error: "Invalid input" });
  }

  try{
    const existingUser = await prisma.user.findUnique({
      where: { email: body.email},
    });
    if (existingUser) {
      c.status(400);
      return c.json({ error: "Email is already in use" });
    }
    const usernameExists = await prisma.user.findUnique({
      where: { username: body.username },
    });
    if (usernameExists) {
      c.status(400);
      return c.json({ error: "Username is already in use" });
    }

    const saltrounds = 10;
    const passwordHash = await bcrypt.hash(body.password, saltrounds);
    const user=await prisma.user.create({
      data:{
        email:body.email,
        passwordHash:passwordHash,
        username:body.username,
      }
    });
    const token = await sign({ userId: user.id }, c.env.JWT_SECRET);
    return c.json({token});
  } catch (e) {
    c.status(500);
    return c.json({ error: "Internal server error" });
  }
});

// Login route
authRouter.post("/login", async (c) => {
    const prisma = new PrismaClient({
      datasourceUrl: c.env.ACCELERATE_URL,
    }).$extends(withAccelerate());


    const body = await c.req.json();

    const parsed=loginSchema.safeParse(body);
    
    if (!parsed.success) {
      c.status(400);
      return c.json({ error: "Invalid input" });
    }
    const data=parsed.data;
    try{
      const existingUser = await prisma.user.findUnique({
        where: { email: data.email ,
        },
      });
      if(!existingUser){
        c.status(400);
        return c.json({ error: "Invalid email or password" });
      }
      const passwordValid = await bcrypt.compare(data.password, existingUser.passwordHash);
      if (!passwordValid) {
        c.status(400);
          return c.json({ error: "Invalid password" });
        }
      const expiry=data.rememberMe ? "30d" : "1d";
      const token =await sign({ userId: existingUser.id , expiry}, c.env.JWT_SECRET);
    return c.json({ token});
  } catch (e) {
    c.status(500);
    return c.json({ error: "Internal server error" });
    }
});

