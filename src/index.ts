import { Hono } from 'hono'
import { authRouter } from './routes/auth.routes';
import { userRouter } from './routes/user.routes';
import { swipeRouter } from './routes/swipe.routes';
import { bookmarkRouter } from './routes/bookmark.routes';
import { discoverRouter } from './routes/discover.routes';
import { collaborationRouter } from './routes/collaboration.routes';

const app = new Hono<{
  Bindings: {
    ACCELERATE_URL: string;
    JWT_SECRET: string;
  },
  Variables: {
    userId: number;
  }
}>();

app.route('/api/auth', authRouter);
app.route("/api/users", userRouter);
app.route("/api/swipes", swipeRouter);
app.route("/api/bookmarks", bookmarkRouter);
app.route("/api/discover", discoverRouter);
app.route("/api/collaborations",collaborationRouter);

export default app
