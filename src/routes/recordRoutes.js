const express = require('express');
const router = express.Router();
const recordController = require('../controllers/recordController');

router.post('/save', recordController.saveRecord);
router.get('/list', recordController.getRecordList);
router.get('/:id', recordController.getRecord);
router.get('/rank/list', recordController.getRank);

module.exports = router;

