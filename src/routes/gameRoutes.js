const express = require('express');
const router = express.Router();
const gameController = require('../controllers/gameController');

router.post('/create', gameController.createGame);
router.get('/:id', gameController.getGame);
router.post('/:id/move', gameController.makeMove);
router.post('/:id/skill', gameController.useSkill);
router.post('/:id/surrender', gameController.surrender);

module.exports = router;

