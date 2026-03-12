/*
  Warnings:

  - You are about to drop the column `qualification` on the `User` table. All the data in the column will be lost.
  - Added the required column `education` to the `User` table without a default value. This is not possible if the table is not empty.
  - Made the column `name` on table `User` required. This step will fail if there are existing NULL values in that column.
  - Made the column `field` on table `User` required. This step will fail if there are existing NULL values in that column.
  - Made the column `organization` on table `User` required. This step will fail if there are existing NULL values in that column.
  - Made the column `experienceYears` on table `User` required. This step will fail if there are existing NULL values in that column.

*/
-- AlterTable
ALTER TABLE "User" DROP COLUMN "qualification",
ADD COLUMN "education" TEXT NOT NULL,
ADD COLUMN "numberOfPapers" INTEGER NOT NULL DEFAULT 0,
ALTER COLUMN "name" SET NOT NULL,
ALTER COLUMN "field" SET NOT NULL,
ALTER COLUMN "organization" SET NOT NULL,
ALTER COLUMN "experienceYears" SET NOT NULL,
ALTER COLUMN "experienceYears" SET DEFAULT 0;
