package chess;

import java.util.HashSet;
import java.util.Set;
import javax.swing.JOptionPane;

public class ChessModel {
	private Set<ChessPiece> piecesBox = new HashSet<ChessPiece>();
	private Player playerInTurn = Player.WHITE;
	private boolean gameEnded = false;

	void reset() {
		piecesBox.clear();
		gameEnded = false;

		for (int i = 0; i < 2; i++) {
			piecesBox.add(new ChessPiece(0 + i * 7, 7, Player.BLACK, Rank.ROOK, ChessConstants.bRook));
			piecesBox.add(new ChessPiece(0 + i * 7, 0, Player.WHITE, Rank.ROOK, ChessConstants.wRook));

			piecesBox.add(new ChessPiece(1 + i * 5, 7, Player.BLACK, Rank.KNIGHT, ChessConstants.bKnight));
			piecesBox.add(new ChessPiece(1 + i * 5, 0, Player.WHITE, Rank.KNIGHT, ChessConstants.wKnight));

			piecesBox.add(new ChessPiece(2 + i * 3, 7, Player.BLACK, Rank.BISHOP, ChessConstants.bBishop));
			piecesBox.add(new ChessPiece(2 + i * 3, 0, Player.WHITE, Rank.BISHOP, ChessConstants.wBishop));
		}

		for (int i = 0; i < 8; i++) {
			piecesBox.add(new ChessPiece(i, 6, Player.BLACK, Rank.PAWN, ChessConstants.bPawn));
			piecesBox.add(new ChessPiece(i, 1, Player.WHITE, Rank.PAWN, ChessConstants.wPawn));
		}

		piecesBox.add(new ChessPiece(3, 7, Player.BLACK, Rank.QUEEN, ChessConstants.bQueen));
		piecesBox.add(new ChessPiece(3, 0, Player.WHITE, Rank.QUEEN, ChessConstants.wQueen));
		piecesBox.add(new ChessPiece(4, 7, Player.BLACK, Rank.KING, ChessConstants.bKing));
		piecesBox.add(new ChessPiece(4, 0, Player.WHITE, Rank.KING, ChessConstants.wKing));

		playerInTurn = Player.WHITE;
	}

	void movePiece(int fromCol, int fromRow, int toCol, int toRow) {
		ChessPiece movingPiece = pieceAt(fromCol, fromRow);
		if (gameEnded) {
			return;
		}
		if (movingPiece == null || (fromCol == toCol && fromRow == toRow)) {
			return;
		}

		// Kiểm tra xem đến lượt của người chơi đúng không
		if (!isPlayerInTurn(movingPiece.getPlayer())) {
			return;
		}

		ChessPiece target = pieceAt(toCol, toRow);
		if (target != null) {
			if (target.getPlayer() == movingPiece.getPlayer()) {
				return; // Không thể ăn quân cùng màu
			} else {
				// Áp dụng luật đi của cờ vua - quân cờ của đối phương có thể bị ăn
				if (!isValidCaptureMove(movingPiece, toCol, toRow)) {
					return; // Nếu không hợp lệ, không thực hiện nước đi
				}
				piecesBox.remove(target);

				// Kiểm tra xem vua của đối phương có bị ăn không
				if (target.getRank() == Rank.KING) {
					checkmate(target.getPlayer());
				}
			}
		} else {
			// Áp dụng luật đi của cờ vua - kiểm tra xem nước đi có hợp lệ không
			if (!isValidRegularMove(movingPiece, fromCol, fromRow, toCol, toRow)) {
				return; // Nếu không hợp lệ, không thực hiện nước đi
			}
		}

		// Thực hiện nước đi và chuyển lượt người chơi
		piecesBox.remove(movingPiece);
		piecesBox.add(new ChessPiece(toCol, toRow, movingPiece.getPlayer(), movingPiece.getRank(), movingPiece.getImgName()));
		playerInTurn = (playerInTurn == Player.WHITE) ? Player.BLACK : Player.WHITE;
	}

	private boolean isValidCaptureMove(ChessPiece movingPiece, int toCol, int toRow) {
		// Kiểm tra nếu có quân cờ đối thủ đứng giữa đường đi
		if (!isPathClear(movingPiece.getCol(), movingPiece.getRow(), toCol, toRow)) {
			return false;
		}

		// Thực hiện kiểm tra các luật cụ thể cho việc ăn quân
		// Cụ thể hóa cho từng loại quân cờ
		switch (movingPiece.getRank()) {
			case PAWN:
				return isValidPawnCaptureMove(movingPiece, toCol, toRow);
			case ROOK:
				return isValidRookMove(movingPiece, toCol, toRow);
			case KNIGHT:
				return isValidKnightMove(movingPiece, toCol, toRow);
			case BISHOP:
				return isValidBishopMove(movingPiece, toCol, toRow);
			case QUEEN:
				return isValidQueenMove(movingPiece, toCol, toRow);
			case KING:
				return isValidKingMove(movingPiece, toCol, toRow);
			default:
				return false;
		}
	}

	private boolean isValidRegularMove(ChessPiece movingPiece, int fromCol, int fromRow, int toCol, int toRow) {
		if (movingPiece.getRank() == Rank.KNIGHT) {
			return isValidKnightMove(movingPiece, toCol, toRow);
		}
		if (isPathClear(fromCol, fromRow, toCol, toRow)) {
			switch (movingPiece.getRank()) {
				case PAWN:
					return isValidPawnRegularMove(movingPiece, toCol, toRow);
				case ROOK:
					return isValidRookMove(movingPiece, toCol, toRow);
				case BISHOP:
					return isValidBishopMove(movingPiece, toCol, toRow);
				case QUEEN:
					return isValidQueenMove(movingPiece, toCol, toRow);
				case KING:
					return isValidKingMove(movingPiece, toCol, toRow);
				default:
					return false;
			}
		}

		return false;
	}

	private boolean isValidPawnCaptureMove(ChessPiece movingPiece, int toCol, int toRow) {
		int direction = (movingPiece.getPlayer() == Player.WHITE) ? 1 : -1;
		return Math.abs(toCol - movingPiece.getCol()) == 1 &&
				toRow - movingPiece.getRow() == direction;
	}

	private boolean isValidPawnRegularMove(ChessPiece movingPiece, int toCol, int toRow) {
		int direction = (movingPiece.getPlayer() == Player.WHITE) ? 1 : -1;
		int rowDifference = toRow - movingPiece.getRow();
		int colDifference = Math.abs(toCol - movingPiece.getCol());

		// Nếu tốt di chuyển một ô về phía trước và ô đó không có quân cờ
		if (colDifference == 0 && rowDifference == direction) {
			return pieceAt(toCol, toRow) == null;
		}

		// Nếu tốt ở vị trí ban đầu và di chuyển hai ô về phía trước và ô đó không có quân cờ
		if (colDifference == 0 && rowDifference == 2 * direction && movingPiece.getRow() == (movingPiece.getPlayer() == Player.WHITE ? 1 : 6)) {
			return pieceAt(toCol, toRow) == null && pieceAt(toCol, movingPiece.getRow() + direction) == null;
		}

		return false;
	}

	// Phương thức kiểm tra xem có quân cờ nào ở giữa không
	private boolean isPathClear(int fromCol, int fromRow, int toCol, int toRow) {
		int colDifference = Math.abs(toCol - fromCol);
		int rowDifference = Math.abs(toRow - fromRow);

		// Nếu đang xử lý cho con mã, không cần kiểm tra đường đi
		if (colDifference == 2 && rowDifference == 1 || colDifference == 1 && rowDifference == 2) {
			return true;
		}

		// Nếu không phải là con mã, thực hiện kiểm tra như bình thường
		int colDirection = Integer.compare(toCol, fromCol);
		int rowDirection = Integer.compare(toRow, fromRow);

		int col = fromCol + colDirection;
		int row = fromRow + rowDirection;

		while (col != toCol || row != toRow) {
			if (pieceAt(col, row) != null) {
				return false; // Có quân cờ nằm giữa đường đi
			}
			col += colDirection;
			row += rowDirection;
		}

		return true; // Không có quân cờ nào nằm giữa đường đi
	}

	// Các phương thức kiểm tra hợp lệ cho từng loại quân cờ
	private boolean isValidRookMove(ChessPiece movingPiece, int toCol, int toRow) {
		return toCol == movingPiece.getCol() || toRow == movingPiece.getRow();
	}

	private boolean isValidKnightMove(ChessPiece movingPiece, int toCol, int toRow) {
		int colDifference = Math.abs(toCol - movingPiece.getCol());
		int rowDifference = Math.abs(toRow - movingPiece.getRow());
		return (colDifference == 2 && rowDifference == 1) || (colDifference == 1 && rowDifference == 2);
	}

	private boolean isValidBishopMove(ChessPiece movingPiece, int toCol, int toRow) {
		return Math.abs(toCol - movingPiece.getCol()) == Math.abs(toRow - movingPiece.getRow());
	}

	private boolean isValidQueenMove(ChessPiece movingPiece, int toCol, int toRow) {
		return isValidRookMove(movingPiece, toCol, toRow) || isValidBishopMove(movingPiece, toCol, toRow);
	}

	private boolean isValidKingMove(ChessPiece movingPiece, int toCol, int toRow) {
		int colDifference = Math.abs(toCol - movingPiece.getCol());
		int rowDifference = Math.abs(toRow - movingPiece.getRow());
		return colDifference <= 1 && rowDifference <= 1;
	}

	ChessPiece pieceAt(int col, int row) {
		for (ChessPiece chessPiece : piecesBox) {
			if (chessPiece.getCol() == col && chessPiece.getRow() == row) {
				return chessPiece;
			}
		}
		return null;
	}

	public boolean isPlayerInTurn(Player player) {
		return player == playerInTurn;
	}

	private void checkmate(Player player) {
		// Hiển thị thông báo về việc ăn vua và reset lại bàn cờ
		Player winner = (player == Player.WHITE) ? Player.BLACK : Player.WHITE;
		JOptionPane.showMessageDialog(null, "Checkmate! Player " + winner + " wins!");
		gameEnded = true;
		//reset();
	}

	@Override
	public String toString() {
		String desc = "";

		for (int row = 7; row >= 0; row--) {
			desc += "" + row;
			for (int col = 0; col < 8; col++) {
				ChessPiece p = pieceAt(col, row);
				if (p == null) {
					desc += " .";
				} else {
					desc += " ";
					switch (p.getRank()) {
						case KING:
							desc += p.getPlayer() == Player.WHITE ? "k" : "K";
							break;
						case QUEEN:
							desc += p.getPlayer() == Player.WHITE ? "q" : "Q";
							break;
						case BISHOP:
							desc += p.getPlayer() == Player.WHITE ? "b" : "B";
							break;
						case ROOK:
							desc += p.getPlayer() == Player.WHITE ? "r" : "R";
							break;
						case KNIGHT:
							desc += p.getPlayer() == Player.WHITE ? "n" : "N";
							break;
						case PAWN:
							desc += p.getPlayer() == Player.WHITE ? "p" : "P";
							break;
					}
				}
			}
			desc += "\n";
		}
		desc += "  0 1 2 3 4 5 6 7";

		return desc;
	}
}
