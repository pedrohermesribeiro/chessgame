const pieceImages = {
  'WHITE_PAWN': 'wp.svg',
  'WHITE_ROOK': 'wr.svg',
  'WHITE_KNIGHT': 'wn.svg',
  'WHITE_BISHOP': 'wb.svg',
  'WHITE_QUEEN': 'wq.svg',
  'WHITE_KING': 'wk.svg',
  'BLACK_PAWN': 'bp.svg',
  'BLACK_ROOK': 'br.svg',
  'BLACK_KNIGHT': 'bn.svg',
  'BLACK_BISHOP': 'bb.svg',
  'BLACK_QUEEN': 'bq.svg',
  'BLACK_KING': 'bk.svg'
};

const files = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'];
const ranks = [8, 7, 6, 5, 4, 3, 2, 1];
let currentBoardData = {};

window.createBoard = function(boardData) {
  console.log('createBoard chamado com:', boardData);
  const board = document.getElementById('chessboard');
  board.innerHTML = '';
  currentBoardData = boardData;

  ranks.forEach((row, rowIndex) => {
      files.forEach((col, colIndex) => {
          const pos = col + row;
          const square = document.createElement('div');
          square.classList.add('square');
          square.dataset.pos = pos;

          const isLight = (colIndex + rowIndex) % 2 === 0;
          square.classList.add(isLight ? 'light' : 'dark');

          const piece = boardData[pos];
          if (piece) {
              const img = document.createElement('img');
              img.src = `/img/${pieceImages[`${piece.color}_${piece.type}`]}`;
              img.width = 48;
              img.height = 48;
              square.appendChild(img);
          }

          // Teste básico de clique
          square.addEventListener('click', () => {
              console.log(`Clique detectado em: ${pos}`);
              square.classList.toggle('selected'); // Alterna a seleção para teste visual
          });

          board.appendChild(square);
      });
  });
  console.log('Tabuleiro renderizado com sucesso');
};