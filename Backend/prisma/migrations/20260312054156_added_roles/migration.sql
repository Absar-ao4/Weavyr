-- CreateEnum
CREATE TYPE "Role" AS ENUM ('PEER', 'MENTOR', 'MENTEE');

-- AlterTable
ALTER TABLE "Paper" ADD COLUMN     "roles" "Role"[] DEFAULT ARRAY[]::"Role"[];
