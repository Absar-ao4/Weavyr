/*
  Warnings:

  - A unique constraint covering the columns `[emailVerificationToken]` on the table `User` will be added. If there are existing duplicate values, this will fail.

*/
-- AlterTable
ALTER TABLE "User" ADD COLUMN     "emailVerificationToken" TEXT,
ADD COLUMN     "isEmailVerified" BOOLEAN NOT NULL DEFAULT false;

-- CreateIndex
CREATE UNIQUE INDEX "User_emailVerificationToken_key" ON "User"("emailVerificationToken");
