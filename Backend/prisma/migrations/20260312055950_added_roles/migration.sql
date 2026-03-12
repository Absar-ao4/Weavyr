/*
  Warnings:

  - You are about to drop the column `roles` on the `Paper` table. All the data in the column will be lost.

*/
-- AlterTable
ALTER TABLE "Paper" DROP COLUMN "roles";

-- AlterTable
ALTER TABLE "User" ADD COLUMN     "roles" "Role"[] DEFAULT ARRAY[]::"Role"[];
