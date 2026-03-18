
#  Weavyr

A modern, scalable full-stack application focused on clean user experience, seamless onboarding, and a robust serverless backend architecture.

---

##  Overview

Weavyr is a user-centric platform designed to deliver smooth authentication, structured onboarding, and efficient profile management. It leverages edge computing and scalable infrastructure to ensure performance and maintainability.

---

##  Features

###  Authentication & Security
- JWT-based authentication
- Secure login and signup flows
- Google OAuth integration (minimal UI)
- Input validation using Zod

###  User Onboarding
- Multi-step profile creation flow
- Structured data collection (username, email, etc.)
- Smooth navigation across onboarding stages

###  UI/UX
- Built with Jetpack Compose
- Clean, minimal, dark-themed interface
- Consistent design system
- Edge-to-edge layout support

###  Backend Architecture
- Serverless backend using Cloudflare Workers (Hono)
- Type-safe APIs with TypeScript
- Prisma ORM for database operations
- PostgreSQL (NeonDB)
- Prisma Accelerate for connection pooling

###  System Design
- Modular architecture
- Clear separation of concerns
- Scalable and maintainable codebase

---

##  Tech Stack

### Frontend
- Kotlin
- Jetpack Compose
- Android Navigation

### Backend
- Node.js (Cloudflare Workers)
- Hono Framework
- TypeScript

### Database
- PostgreSQL (NeonDB)
- Prisma ORM
- Prisma Accelerate

### Auth & Validation
- JWT
- Zod

---

## 📂 Project Structure

```
frontend/
├── ui/
├── screens/
├── navigation/
└── theme/

backend/
├── routes/
├── controllers/
├── services/
├── middleware/
├── utils/
└── prisma/
```


---

## ⚙️ Setup & Installation

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/weavyr.git
cd weavyr
cd backend
npm install
npx prisma generate
npx prisma migrate dev
```
Create a .env file:
```
DATABASE_URL=your_neon_db_url
JWT_SECRET=your_secret
npm run dev
```
##  API Design Principles

- RESTful architecture  
- Input validation at entry points  
- Stateless authentication  
- Structured error handling  

---

##  Current Status

- Authentication flow implemented  
- Onboarding UI in development  
- Backend architecture stable  
- Google OAuth integration in progress  

---

##  Future Improvements

- Role-based access control  
- Real-time features  
- Notification system  
- Analytics dashboard  
- CI/CD pipeline  

---

##  Contributing

Contributions are welcome:

1. Fork the repository  
2. Create a new branch  
3. Commit your changes  
4. Open a pull request  

---

##  License

This project is licensed under the MIT License.

