/*
  Warnings:

  - A unique constraint covering the columns `[paperUrl]` on the table `Paper` will be added. If there are existing duplicate values, this will fail.
  - Made the column `paperUrl` on table `Paper` required. This step will fail if there are existing NULL values in that column.

*/
-- AlterTable
ALTER TABLE "Paper" ALTER COLUMN "paperUrl" SET NOT NULL;

-- CreateIndex
CREATE UNIQUE INDEX "Paper_paperUrl_key" ON "Paper"("paperUrl");
