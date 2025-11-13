/**
 * 错误处理中间件
 */
module.exports = (err, req, res, next) => {
  console.error('❌ 错误:', err);

  const statusCode = err.statusCode || 500;
  const message = err.message || '服务器内部错误';

  res.status(statusCode).json({
    success: false,
    message,
    ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
  });
};

