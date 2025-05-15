# (c) https://github.com/MontiCore/monticore
import logging
import sys
import os
import reinforcement_learning.util as util


class ArchLogger(object):
    _logger = None

    __output_level = logging.INFO
    __logger_name = 'agent'
    __output_directory = '.'
    __append = True
    __logformat = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    __dateformat = '%d-%b-%y %H:%M:%S'

    INFO = logging.INFO
    DEBUG = logging.DEBUG

    @staticmethod
    def set_output_level(output_level):
        assert output_level is not None
        ArchLogger.__output_level = output_level

    @staticmethod
    def set_logger_name(logger_name):
        assert logger_name is not None
        ArchLogger.__logger_name = logger_name

    @staticmethod
    def set_output_directory(output_directory):
        assert output_directory is not None
        ArchLogger.__output_directory = output_directory

    @staticmethod
    def set_append(append):
        assert append is not None
        ArchLogger.__append = append

    @staticmethod
    def set_log_format(logformat, dateformat):
        assert logformat is not None
        assert dateformat is not None
        ArchLogger.__logformat = logformat
        ArchLogger.__dateformat = dateformat

    @staticmethod
    def init_logger(make_log_file=True):
        assert ArchLogger._logger is None, 'Logger init already called'
        filemode = 'a' if ArchLogger.__append else 'w'
        formatter = logging.Formatter(
            fmt=ArchLogger.__logformat, datefmt=ArchLogger.__dateformat)

        logger = logging.getLogger(ArchLogger.__logger_name)
        logger.propagate = False

        if not logger.handlers:
            logger.setLevel(ArchLogger.__output_level)
            stream_handler = logging.StreamHandler(sys.stdout)
            stream_handler.setLevel(ArchLogger.__output_level)
            stream_handler.setFormatter(formatter)
            logger.addHandler(stream_handler)

            if make_log_file:
                util.make_directory_if_not_exist(ArchLogger.__output_directory)
                log_file = os.path.join(
                    ArchLogger.__output_directory,
                    ArchLogger.__logger_name + '.log')
                file_handler = logging.FileHandler(log_file, mode=filemode)
                file_handler.setLevel(ArchLogger.__output_level)
                file_handler.setFormatter(formatter)
                logger.addHandler(file_handler)
        ArchLogger._logger = logger

    @staticmethod
    def get_logger():
        if ArchLogger._logger is None:
            ArchLogger.init_logger()
        assert ArchLogger._logger is not None
        return ArchLogger._logger

if __name__ == "__main__":
    print('=== Test logger ===')
    ArchLogger.set_logger_name('TestLogger')
    ArchLogger.set_output_directory('test_log')
    ArchLogger.init_logger()
    logger = ArchLogger.get_logger()
    logger.warning('This is a warning')
    logger.debug('This is a debug information, which you should not see')
    logger.info('This is a normal information')
    assert os.path.exists('test_log')\
        and os.path.isfile(os.path.join('test_log', 'TestLogger.log')),\
        'Test failed: No logfile exists'
    import shutil
    shutil.rmtree('test_log')
