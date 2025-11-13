const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');

router.post('/login', userController.login);
router.get('/:userId', userController.getUserInfo);
router.put('/:userId', userController.updateUserInfo);
router.get('/:userId/stats', userController.getUserStats);

module.exports = router;

