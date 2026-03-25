/*
  Warnings:

  - You are about to drop the `PushToken` table. If the table is not empty, all the data it contains will be lost.

*/
-- DropForeignKey
ALTER TABLE "PushToken" DROP CONSTRAINT "PushToken_userId_fkey";

-- DropTable
DROP TABLE "PushToken";
