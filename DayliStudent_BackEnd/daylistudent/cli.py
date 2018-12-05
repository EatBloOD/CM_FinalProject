import logging

import click
import click_log

from daylistudent import server
from daylistudent.server import setup_logging

logger = logging.getLogger(__name__)


@click.group()
@click.option('--info', default=False, is_flag=True, help='Logger in info level')
@click.option('--debug', default=False, is_flag=True, help='Logger in debug level')
@click.option('--error', default=False, is_flag=True, help='Logger in error level')
@click_log.simple_verbosity_option(logger)
@click.version_option()
def cli(info, debug, error):
    """ DayliStudent is a back end server for the DayliStudent app """
    if not info and not debug and not error:
        setup_logging(logging_level=logging.NOTSET)
        logger.info('logging_level: NOTSET')
    if info:
        setup_logging(logging_level=logging.INFO)
        logger.info('logging_level: INFO')
    if debug:
        setup_logging(logging_level=logging.DEBUG)
        logger.info('logging_level: DEBUG')
    if error:
        setup_logging(logging_level=logging.ERROR)
        logger.info('logging_level: ERROR')


@cli.command('run')
def run():
    """ The `run` command, executes Dayli Student in the local machine """
    server.run()
