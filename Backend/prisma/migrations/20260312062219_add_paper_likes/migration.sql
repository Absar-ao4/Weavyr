-- CreateTable
CREATE TABLE "PaperLike" (
    "id" SERIAL NOT NULL,
    "userId" INTEGER NOT NULL,
    "paperId" INTEGER NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "PaperLike_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "PaperLike_userId_paperId_key" ON "PaperLike"("userId", "paperId");

-- AddForeignKey
ALTER TABLE "PaperLike" ADD CONSTRAINT "PaperLike_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "PaperLike" ADD CONSTRAINT "PaperLike_paperId_fkey" FOREIGN KEY ("paperId") REFERENCES "Paper"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
