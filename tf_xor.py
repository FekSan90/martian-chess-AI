# 6.1 Example: Learning XOR - GBC Book - Chapter 6 - pp. 166 to 171
# Some parts are inspired by the blog post
# Solving XOR with a Neural Network in TensorFlow
# by Stephen OMAN
# https://github.com/StephenOman/TensorFlowExamples/blob/master/xor%20nn/xor_nn.py

# Activation RELU + sigmoid for binary classification output + MSE loss function
import tensorflow as tf
import time
import numpy as np

# The next step is to set up placeholders to hold the input data.
# TensorFlow will automatically fill them with the data when we run the network
# In our XOR problem, we have 4 different training examples and each example has 2 features. - X => [4,2]
# There are also 4 expected outputs, each with just 1 value (either a 0 or 1) - Y => [4,1]
X = tf.placeholder(tf.float32, shape=[4,2], name = 'X')
Y = tf.placeholder(tf.float32, shape=[4,1], name = 'Y')

# Set up the parameters for the network. These are called Variables in TensorFlow.
# Variables will be modified by TensorFlow during the training steps
# Weights
W = tf.Variable(tf.truncated_normal([2,2]), name = "W") # random values from a truncated normal distribution with [2,2] shape (mean = 0, std = 1).
w = tf.Variable(tf.truncated_normal([2,1]), name = "w") # random values from a truncated normal distribution with [2,1] shape(mean = 0, std = 1).
# Biases
c = tf.Variable(tf.zeros([4,2]), name = "c") # zeros with [4,2] shape
b = tf.Variable(tf.zeros([4,1]), name = "b") # zeros with [4,1] shape

# Set up the model with RELU activation functions = X*W + c

with tf.name_scope("hidden_layer") as scope:
    h = tf.nn.relu(tf.add(tf.matmul(X, W),c))

# Set the output with sigmoid function = h*w + b

with tf.name_scope("output") as scope:
    y_estimated = tf.sigmoid(tf.add(tf.matmul(h,w),b))

# Cost function is
# tf.reduce_mean Computes the mean of elements across dimensions of a tensor
with tf.name_scope("loss") as scope:
    loss = tf.reduce_mean(tf.squared_difference(y_estimated, Y)) 

# For better result with binary classifier, use cross entropy with a sigmoid
#    loss = tf.nn.sigmoid_cross_entropy_with_logits(logits=y_estimated, labels=Y)

# A naive direct implementation of the loss function
#     n_instances = X.get_shape().as_list()[0]
#     loss = tf.reduce_sum(tf.pow(y_estimated - Y, 2))/ n_instances

# In case of problem with gradient (exploding or vanishing gradient)perform gradient clipping
#     n_instances = X.get_shape().as_list()[0]
#     loss = tf.reduce_sum(tf.pow(tf.clip_by_value(y_estimated,1e-10,1.0) - Y,2))/(n_instances)

# TensorFlow ships with several different training algorithms
# We are going to use the gradient descent algorithm:
# 0.01 is the learning rate, and the goal is to minimize the loss
with tf.name_scope("train") as scope:
    train_step = tf.train.GradientDescentOptimizer(0.01).minimize(loss)

# Initialization step

INPUT_XOR = [[0,0],[0,1],[1,0],[1,1]]
OUTPUT_XOR = [[0],[1],[1],[0]]
# initializes global variables in the graph
init = tf.global_variables_initializer()
# A Session object encapsulates the environment in which Operation objects are executed, and Tensor objects are evaluated
sess = tf.Session()
# write a graph log, which can be visualized trough Tensorboard (tensor_XOR>python -m tensorboard.main --logdir=<PATHTODIR>\logs\xor_logs) 
# and a browser using URL: localhost:6006
writer = tf.summary.FileWriter("./logs/xor_logs", sess.graph)

# TensorFlow runs a model inside a session, which it uses to maintain the state of the variables as they are passed through the network we have set up.
# So the first step in that session is to initialise all the Variables from above. 
# This step allocates values to the various Variables in accordance with how we set them up 

sess.run(init)

t_start = time.clock()
for epoch in range(100001):
    sess.run(train_step, feed_dict={X: INPUT_XOR, Y: OUTPUT_XOR})
    if epoch % 10000 == 0:
	# Each time the training step is executed, the values in the dictionary feed_dict are loaded into the placeholders that we set up at the beginning. 
	# As the XOR problem is relatively simple, each epoch will contain the entire training set INPUT_XOR
        print("_"*80)
        print('Epoch: ', epoch)
        print('   y_estimated: ')
        for element in sess.run(y_estimated, feed_dict={X: INPUT_XOR, Y: OUTPUT_XOR}):
            print('    ',element)
        print('   W: ')
        for element in sess.run(W):
            print('    ',element)
        print('   c: ')
        for element in sess.run(c):
            print('    ',element)
        print('   w: ')
        for element in sess.run(w):
            print('    ',element)
        print('   b ')
        for element in sess.run(b):
            print('    ',element)
        print('   loss: ', sess.run(loss, feed_dict={X: INPUT_XOR, Y: OUTPUT_XOR}))
t_end = time.clock()
print("_"*80)
print('Elapsed time ', t_end - t_start)
