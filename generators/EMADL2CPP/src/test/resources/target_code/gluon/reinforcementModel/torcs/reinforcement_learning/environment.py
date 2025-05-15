# (c) https://github.com/MontiCore/monticore
import abc
import logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)
import torcs_agent_dqn_reward_executor

class RewardFunction(object):
    def __init__(self):
        self.__reward_wrapper = torcs_agent_dqn_reward_executor.torcs_agent_dqn_reward_executor()
        self.__reward_wrapper.init()

    def reward(self, state, terminal):
        s = state.astype('double')
        t = bool(terminal)
        inp = torcs_agent_dqn_reward_executor.torcs_agent_dqn_reward_input()
        inp.state = s
        inp.isTerminal = t
        output = self.__reward_wrapper.execute(inp)
        return output.reward



class Environment:
    __metaclass__ = abc.ABCMeta

    def __init__(self):
        self._reward_function = RewardFunction()

    @abc.abstractmethod
    def reset(self):
        pass

    @abc.abstractmethod
    def step(self, action):
        pass

    @abc.abstractmethod
    def close(self):
        pass

import rospy
import sys
if sys.version.strip().split(".")[0] > '2':
    import _thread as thread
else:
    import thread
import numpy as np
import time
from std_msgs.msg import Float32MultiArray, Bool, Int32, MultiArrayDimension, Float32

class RosEnvironment(Environment):
    def __init__(self,
        ros_node_name='RosTrainingAgent',
        timeout_in_s=60,
        state_topic='state',
        action_topic='action',
        reset_topic='reset',
        terminal_state_topic='terminal',
        reward_topic='reward'):
        super(RosEnvironment, self).__init__()
        self.__timeout_in_s = timeout_in_s
        self.__in_reset = False
        self.__waiting_for_state_update = False
        self.__waiting_for_terminal_update = False
        self.__last_received_state = 0
        self.__last_received_terminal = True

        rospy.loginfo("Initialize node {0}".format(ros_node_name))

        self.__step_publisher = rospy.Publisher(action_topic, Int32, queue_size=1)
        rospy.loginfo('Step Publisher initialized with topic {}'.format(action_topic))

        self.__reset_publisher = rospy.Publisher(reset_topic, Bool, queue_size=1)
        rospy.loginfo('Reset Publisher initialized with topic {}'.format(reset_topic))

        rospy.init_node(ros_node_name, anonymous=True)

        self.__state_subscriber = rospy.Subscriber(state_topic, Float32MultiArray, self.__state_callback)
        rospy.loginfo('State Subscriber registered with topic {}'.format(state_topic))

        self.__terminal_state_subscriber = rospy.Subscriber(terminal_state_topic, Bool, self.__terminal_state_callback)
        rospy.loginfo('Terminal State Subscriber registered with topic {}'.format(terminal_state_topic))

        rate = rospy.Rate(10)

        thread.start_new_thread(rospy.spin, ())
        time.sleep(2)

    def reset(self):
        self.__in_reset = True
        reset_message = Bool()
        reset_message.data = True
        self.__waiting_for_state_update = True
        self.__waiting_for_terminal_update = False
        self.__waiting_for_reward_update = False
        self.__reset_publisher.publish(reset_message)
        self.__wait_for_new_state(self.__reset_publisher, reset_message)
        while self.__last_received_terminal:
            pass
        self.__in_reset = False
        return self.__last_received_state

    def step(self, action):
        action_rospy = Int32()
        action_rospy.data = action

        logger.debug('Send action: {}'.format(action))

        self.__waiting_for_state_update = True
        self.__waiting_for_terminal_update = True
        self.__step_publisher.publish(action_rospy)
        self.__wait_for_new_state(self.__step_publisher, action_rospy)
        next_state = self.__last_received_state
        terminal = self.__last_received_terminal
        reward = self.__calc_reward(next_state, terminal)

        logger.debug('Transition: ({}, {}, {}, {})'.format(action, reward, next_state, terminal))

        return next_state, reward, terminal, 0

    def __wait_for_new_state(self, publisher, msg):
        time_of_timeout = time.time() + self.__timeout_in_s
        timeout_counter = 0
        while(self.__waiting_for_state_update
              or self.__waiting_for_terminal_update):
            is_timeout = (time.time() > time_of_timeout)
            if (is_timeout):
                if timeout_counter < 3:
                    rospy.logwarn("Timeout occured: Retry message")
                    publisher.publish(msg)
                    timeout_counter += 1
                    time_of_timeout = time.time() + self.__timeout_in_s
                else:
                    rospy.logerr("Timeout 3 times in a row: Terminate application")
                    exit()
            time.sleep(1/500)

    def close(self):
        rospy.signal_shutdown('Program ended!')

    def __state_callback(self, data):
        self.__last_received_state = np.array(data.data, dtype='float32').reshape((5,))
        logger.debug('Received state: {}'.format(self.__last_received_state))
        self.__waiting_for_state_update = False

    def __terminal_state_callback(self, data):
        self.__last_received_terminal = np.bool(data.data)
        logger.debug('Received terminal flag: {}'.format(self.__last_received_terminal))
        self.__waiting_for_terminal_update = False

    def __calc_reward(self, state, terminal):
        # C++ Wrapper call
        return self._reward_function.reward(state, terminal)
